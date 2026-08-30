package io.github.tracerxbrhd.thecoinage.gameplay;

import io.github.tracerxbrhd.thecoinage.api.CoinageEvents;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyBreakdown;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.config.CoinageServerConfig;
import io.github.tracerxbrhd.thecoinage.data.CoinageDataRegistry;
import io.github.tracerxbrhd.thecoinage.purse.ActivePurseResolver;
import io.github.tracerxbrhd.thecoinage.purse.PurseContents;
import io.github.tracerxbrhd.thecoinage.purse.PurseHandle;
import io.github.tracerxbrhd.thecoinage.purse.PurseStorage;
import io.github.tracerxbrhd.thecoinage.registry.CoinageItems;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

public final class CoinageGameplayEvents {
    private CoinageGameplayEvents() {}

    @SubscribeEvent
    public static void onCoinPickup(ItemEntityPickupEvent.Pre event) {
        if (!CoinageServerConfig.AUTOMATIC_COIN_PICKUP.get() || !(event.getPlayer() instanceof ServerPlayer player)) return;
        ItemEntity entity = event.getItemEntity();
        ItemStack loose = entity.getItem();
        Denomination denomination = CoinageItems.denomination(loose);
        if (denomination == null || loose.isEmpty()) return;
        Optional<PurseHandle> resolved = ActivePurseResolver.find(player);
        if (resolved.isEmpty() || !resolved.get().isValid()) return;

        PurseHandle handle = resolved.get();
        ItemStack replacement = handle.get().copy();
        PurseContents before = PurseStorage.read(replacement);
        long capacity = CoinageServerConfig.PURSE_CAPACITY.getAsLong();
        if (before.validate(capacity) != PurseContents.PurseValidation.VALID) return;
        PurseStorage.DepositResult deposit = PurseStorage.deposit(replacement, denomination, loose.getCount(), capacity);
        if (deposit.accepted() <= 0 || !handle.set(replacement)) return;

        loose.shrink((int) deposit.accepted());
        player.take(entity, (int) deposit.accepted());
        player.awardStat(Stats.ITEM_PICKED_UP.get(CoinageItems.coin(denomination)), (int) deposit.accepted());
        player.displayClientMessage(Component.translatable("message.the_coinage.received",
            deposit.accepted(), Component.translatable(denomination.translationKey())), true);
        NeoForge.EVENT_BUS.post(new CoinageEvents.PurseChanged(player, before.breakdown(),
            PurseStorage.read(replacement).breakdown()));
        NeoForge.EVENT_BUS.post(new CoinageEvents.CurrencyReceived(player,
            CurrencyBreakdown.ZERO.with(denomination, deposit.accepted())));
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!CoinageServerConfig.MOB_COIN_DROPS.get() || !event.isRecentlyHit() || event.getEntity().level().isClientSide()) return;
        for (var definition : CoinageDataRegistry.mobDrops()) {
            if (!definition.matches(event.getEntity()) || event.getEntity().getRandom().nextDouble() >= definition.chance()) continue;
            int count = definition.min() + event.getEntity().getRandom().nextInt(definition.max() - definition.min() + 1);
            while (count > 0) {
                int chunk = Math.min(count, CoinageItems.coin(definition.denomination()).getDefaultMaxStackSize());
                event.getDrops().add(new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(),
                    event.getEntity().getZ(), new ItemStack(CoinageItems.coin(definition.denomination()), chunk)));
                count -= chunk;
            }
        }
    }
}
