package io.github.tracerxbrhd.thecoinage.reward;

import io.github.tracerxbrhd.thecoinage.api.CoinageEvents;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyBreakdown;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.config.CoinageServerConfig;
import io.github.tracerxbrhd.thecoinage.purse.ActivePurseResolver;
import io.github.tracerxbrhd.thecoinage.purse.PurseContents;
import io.github.tracerxbrhd.thecoinage.purse.PurseHandle;
import io.github.tracerxbrhd.thecoinage.purse.PurseStorage;
import io.github.tracerxbrhd.thecoinage.registry.CoinageItems;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

/** Purse -> inventory -> safe world drop reward policy. */
public final class CurrencyRewardService {
    private CurrencyRewardService() {}

    public static CurrencyBreakdown give(ServerPlayer player, CurrencyBreakdown reward) {
        if (reward.isZero()) return CurrencyBreakdown.ZERO;
        synchronized (player) {
            CurrencyBreakdown remainder = reward;
            Optional<PurseHandle> optional = ActivePurseResolver.find(player);
            if (optional.isPresent() && optional.get().isValid()) {
                PurseHandle handle = optional.get();
                ItemStack working = handle.get().copy();
                PurseContents beforeData = PurseStorage.read(working);
                long capacity = CoinageServerConfig.PURSE_CAPACITY.getAsLong();
                if (beforeData.validate(capacity) == PurseContents.PurseValidation.VALID) {
                    CurrencyBreakdown accepted = CurrencyBreakdown.ZERO;
                    for (Denomination denomination : Denomination.values()) {
                        long requested = remainder.get(denomination);
                        PurseStorage.DepositResult result = PurseStorage.deposit(working, denomination, requested, capacity);
                        accepted = accepted.with(denomination, result.accepted());
                        remainder = remainder.with(denomination, result.remainder());
                    }
                    if (!accepted.isZero() && handle.set(working)) {
                        NeoForge.EVENT_BUS.post(new CoinageEvents.PurseChanged(player, beforeData.breakdown(),
                            PurseStorage.read(working).breakdown()));
                    } else if (!accepted.isZero()) {
                        remainder = remainder.plus(accepted);
                    }
                }
            }
            giveLooseOrDrop(player, remainder);
            NeoForge.EVENT_BUS.post(new CoinageEvents.CurrencyReceived(player, reward));
            return reward;
        }
    }

    public static void giveLooseOrDrop(ServerPlayer player, CurrencyBreakdown amount) {
        for (Denomination denomination : Denomination.values()) {
            long remaining = amount.get(denomination);
            while (remaining > 0) {
                int chunk = (int) Math.min(remaining, CoinageItems.coin(denomination).getDefaultMaxStackSize());
                ItemStack stack = new ItemStack(CoinageItems.coin(denomination), chunk);
                player.getInventory().add(stack);
                if (!stack.isEmpty()) player.drop(stack, false);
                remaining -= chunk;
            }
        }
    }
}
