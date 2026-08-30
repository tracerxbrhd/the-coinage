package io.github.tracerxbrhd.thecoinage.payment;

import io.github.tracerxbrhd.thecoinage.api.CoinageEvents;
import io.github.tracerxbrhd.thecoinage.api.PaymentFailure;
import io.github.tracerxbrhd.thecoinage.api.PaymentResult;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyBreakdown;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyMath;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyRules;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.config.CoinageServerConfig;
import io.github.tracerxbrhd.thecoinage.purse.ActivePurseResolver;
import io.github.tracerxbrhd.thecoinage.purse.PurseContents;
import io.github.tracerxbrhd.thecoinage.purse.PurseHandle;
import io.github.tracerxbrhd.thecoinage.purse.PurseStorage;
import io.github.tracerxbrhd.thecoinage.registry.CoinageItems;
import io.github.tracerxbrhd.thecoinage.reward.CurrencyRewardService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

/** Builds complete withdrawal plans before committing any player-owned state. */
public final class PlayerFundsService {
    private PlayerFundsService() {}

    public static CurrencyBreakdown available(ServerPlayer player) {
        try {
            CurrencyBreakdown total = looseBalance(player);
            Optional<PurseHandle> purse = ActivePurseResolver.find(player);
            if (purse.isPresent()) {
                PurseContents data = PurseStorage.read(purse.get().get());
                if (data.validate(CoinageServerConfig.PURSE_CAPACITY.getAsLong()) == PurseContents.PurseValidation.VALID) {
                    total = total.plus(data.breakdown());
                }
            }
            return total;
        } catch (ArithmeticException exception) {
            throw new IllegalStateException("Player currency exceeds supported long range", exception);
        }
    }

    public static PaymentResult simulate(ServerPlayer player, CurrencyBreakdown price) {
        return transact(player, price, true);
    }

    public static PaymentResult pay(ServerPlayer player, CurrencyBreakdown price) {
        return transact(player, price, false);
    }

    private static PaymentResult transact(ServerPlayer player, CurrencyBreakdown price, boolean simulate) {
        if (price == null || price.isZero()) return price == null
            ? PaymentResult.failure(simulate, PaymentFailure.INVALID_REQUEST, CurrencyBreakdown.ZERO)
            : PaymentResult.success(simulate, price);
        synchronized (player) {
            try {
                return CoinageServerConfig.AUTOMATIC_PURSE_CONVERSION.get()
                    ? withConversion(player, price, simulate)
                    : exactDenominations(player, price, simulate);
            } catch (ArithmeticException exception) {
                return PaymentResult.failure(simulate, PaymentFailure.OVERFLOW, price);
            }
        }
    }

    private static PaymentResult exactDenominations(ServerPlayer player, CurrencyBreakdown price, boolean simulate) {
        Optional<PurseHandle> handle = ActivePurseResolver.find(player);
        ItemStack purseSnapshot = handle.map(value -> value.get().copy()).orElse(ItemStack.EMPTY);
        long capacity = CoinageServerConfig.PURSE_CAPACITY.getAsLong();
        PurseContents purseData = handle.map(value -> PurseStorage.read(purseSnapshot)).orElse(PurseContents.EMPTY);
        boolean usablePurse = handle.isPresent() && purseData.validate(capacity) == PurseContents.PurseValidation.VALID;
        CurrencyBreakdown purseTake = CurrencyBreakdown.ZERO;
        List<SlotTake> inventoryTake = new ArrayList<>();

        for (Denomination denomination : Denomination.values()) {
            long remaining = price.get(denomination);
            long fromPurse = usablePurse ? Math.min(remaining, purseData.get(denomination)) : 0;
            purseTake = purseTake.with(denomination, fromPurse);
            remaining -= fromPurse;
            for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (CoinageItems.denomination(stack) != denomination) continue;
                int amount = (int) Math.min(remaining, stack.getCount());
                inventoryTake.add(new SlotTake(slot, stack.copy(), amount));
                remaining -= amount;
            }
            if (remaining > 0) return PaymentResult.failure(simulate, PaymentFailure.MISSING_DENOMINATION, price);
        }

        if (simulate) return PaymentResult.success(true, price);
        if (!revalidate(player, handle, purseSnapshot, inventoryTake)) {
            return PaymentResult.failure(false, PaymentFailure.STATE_CHANGED, price);
        }
        if (usablePurse && !purseTake.isZero()) {
            CurrencyBreakdown before = purseData.breakdown();
            CurrencyBreakdown after = new CurrencyBreakdown(before.copper() - purseTake.copper(),
                before.silver() - purseTake.silver(), before.gold() - purseTake.gold());
            ItemStack replacement = purseSnapshot.copy();
            if (!PurseStorage.replace(replacement, after, capacity) || !handle.orElseThrow().set(replacement)) {
                return PaymentResult.failure(false, PaymentFailure.STATE_CHANGED, price);
            }
            NeoForge.EVENT_BUS.post(new CoinageEvents.PurseChanged(player, before, after));
        }
        for (SlotTake take : inventoryTake) player.getInventory().getItem(take.slot()).shrink(take.amount());
        player.getInventory().setChanged();
        NeoForge.EVENT_BUS.post(new CoinageEvents.CurrencySpent(player, price));
        return PaymentResult.success(false, price);
    }

    private static PaymentResult withConversion(ServerPlayer player, CurrencyBreakdown price, boolean simulate) {
        CurrencyRules rules = CoinageServerConfig.currencyRules();
        long priceValue = price.normalizedValue(rules);
        Optional<PurseHandle> handle = ActivePurseResolver.find(player);
        ItemStack purseSnapshot = handle.map(value -> value.get().copy()).orElse(ItemStack.EMPTY);
        long capacity = CoinageServerConfig.PURSE_CAPACITY.getAsLong();
        PurseContents purseData = handle.map(value -> PurseStorage.read(purseSnapshot)).orElse(PurseContents.EMPTY);
        boolean usablePurse = handle.isPresent() && purseData.validate(capacity) == PurseContents.PurseValidation.VALID;
        long purseValue = usablePurse ? purseData.breakdown().normalizedValue(rules) : 0;
        List<SlotTake> allCoins = coinSnapshots(player);
        long inventoryValue = looseBalance(player).normalizedValue(rules);
        if (Math.addExact(purseValue, inventoryValue) < priceValue) {
            return PaymentResult.failure(simulate, PaymentFailure.INSUFFICIENT_FUNDS, price);
        }
        if (simulate) return PaymentResult.success(true, price);
        if (!revalidate(player, handle, purseSnapshot, allCoins)) {
            return PaymentResult.failure(false, PaymentFailure.STATE_CHANGED, price);
        }

        long purseSpent = Math.min(purseValue, priceValue);
        long inventorySpent = priceValue - purseSpent;
        if (usablePurse && purseSpent > 0) {
            CurrencyBreakdown before = purseData.breakdown();
            CurrencyBreakdown after = CurrencyMath.normalize(purseValue - purseSpent, rules);
            ItemStack replacement = purseSnapshot.copy();
            if (!PurseStorage.replace(replacement, after, capacity) || !handle.orElseThrow().set(replacement)) {
                return PaymentResult.failure(false, PaymentFailure.STATE_CHANGED, price);
            }
            NeoForge.EVENT_BUS.post(new CoinageEvents.PurseChanged(player, before, after));
        }
        if (inventorySpent > 0) {
            long changeValue = inventoryValue - inventorySpent;
            for (SlotTake take : allCoins) player.getInventory().setItem(take.slot(), ItemStack.EMPTY);
            CurrencyRewardService.giveLooseOrDrop(player, CurrencyMath.normalize(changeValue, rules));
        }
        player.getInventory().setChanged();
        NeoForge.EVENT_BUS.post(new CoinageEvents.CurrencySpent(player, price));
        return PaymentResult.success(false, price);
    }

    private static boolean revalidate(ServerPlayer player, Optional<PurseHandle> handle, ItemStack purseSnapshot,
                                      List<SlotTake> slots) {
        if (handle.isPresent() && !ItemStack.matches(handle.get().get(), purseSnapshot)) return false;
        for (SlotTake take : slots) {
            if (!ItemStack.matches(player.getInventory().getItem(take.slot()), take.snapshot())) return false;
        }
        return true;
    }

    private static List<SlotTake> coinSnapshots(ServerPlayer player) {
        List<SlotTake> result = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (CoinageItems.denomination(stack) != null) result.add(new SlotTake(slot, stack.copy(), stack.getCount()));
        }
        return result;
    }

    private static CurrencyBreakdown looseBalance(ServerPlayer player) {
        CurrencyBreakdown balance = CurrencyBreakdown.ZERO;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            Denomination denomination = CoinageItems.denomination(stack);
            if (denomination != null) {
                balance = balance.with(denomination, Math.addExact(balance.get(denomination), stack.getCount()));
            }
        }
        return balance;
    }

    private record SlotTake(int slot, ItemStack snapshot, int amount) {}
}
