package io.github.tracerxbrhd.thecoinage.item;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyMath;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.config.CoinageServerConfig;
import io.github.tracerxbrhd.thecoinage.menu.PurseMenu;
import io.github.tracerxbrhd.thecoinage.purse.PurseContents;
import io.github.tracerxbrhd.thecoinage.purse.PurseStorage;
import io.github.tracerxbrhd.thecoinage.registry.CoinageItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class CoinPurseItem extends Item {
    public CoinPurseItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack purse, Slot slot, ClickAction action, Player player) {
        if (purse.getCount() != 1 || action != ClickAction.SECONDARY) return false;
        ItemStack coins = slot.getItem();
        Denomination denomination = CoinageItems.denomination(coins);
        if (denomination != null) {
            long accepted = PurseStorage.deposit(purse, denomination, coins.getCount(),
                CoinageServerConfig.capacity(denomination)).accepted();
            if (accepted > 0) {
                coins.shrink((int) accepted);
                slot.setChanged();
                playInsert(player);
            }
            return true;
        }
        if (coins.isEmpty()) return extractTo(purse, player, stack -> slot.safeInsert(stack));
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack purse, ItemStack carried, Slot slot, ClickAction action,
                                             Player player, SlotAccess carriedAccess) {
        if (purse.getCount() != 1 || action != ClickAction.SECONDARY || !slot.allowModification(player)) return false;
        Denomination denomination = CoinageItems.denomination(carried);
        if (denomination != null) {
            long accepted = PurseStorage.deposit(purse, denomination, carried.getCount(),
                CoinageServerConfig.capacity(denomination)).accepted();
            if (accepted > 0) {
                carried.shrink((int) accepted);
                playInsert(player);
            }
            return true;
        }
        if (carried.isEmpty()) return extractTo(purse, player, stack -> {
            carriedAccess.set(stack);
            return ItemStack.EMPTY;
        });
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            int purseSlot = hand == InteractionHand.OFF_HAND ? 40 : player.getInventory().selected;
            serverPlayer.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new PurseMenu(containerId, inventory, purseSlot),
                Component.translatable("container.the_coinage.coin_purse")),
                buffer -> buffer.writeVarInt(purseSlot));
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        PurseContents contents = PurseStorage.read(stack);
        long capacity = CoinageServerConfig.PURSE_CAPACITY.getAsLong();
        if (contents.validate(capacity) != PurseContents.PurseValidation.VALID) {
            tooltip.add(Component.translatable("tooltip.the_coinage.purse.invalid").withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("tooltip.the_coinage.purse.format", contents.formatVersion())
                .withStyle(ChatFormatting.DARK_RED));
            return;
        }
        tooltip.add(line(Denomination.COPPER, contents.copper()));
        tooltip.add(line(Denomination.SILVER, contents.silver()));
        tooltip.add(line(Denomination.GOLD, contents.gold()));
        try {
            tooltip.add(Component.translatable("tooltip.the_coinage.purse.total",
                CurrencyMath.formatNormalized(contents.breakdown().normalizedValue(CoinageServerConfig.currencyRules())))
                .withStyle(ChatFormatting.GRAY));
        } catch (ArithmeticException exception) {
            tooltip.add(Component.translatable("tooltip.the_coinage.purse.total_overflow").withStyle(ChatFormatting.RED));
        }
        tooltip.add(Component.translatable("tooltip.the_coinage.purse.controls").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Component line(Denomination denomination, long value) {
        return Component.translatable("tooltip.the_coinage.purse.line",
            Component.translatable(denomination.translationKey()), CurrencyMath.formatNormalized(value))
            .withStyle(ChatFormatting.GRAY);
    }

    private static boolean extractTo(ItemStack purse, Player player,
                                     java.util.function.Function<ItemStack, ItemStack> target) {
        PurseContents contents = PurseStorage.read(purse);
        for (Denomination denomination : new Denomination[] {Denomination.GOLD, Denomination.SILVER, Denomination.COPPER}) {
            if (contents.get(denomination) <= 0) continue;
            int count = (int) Math.min(contents.get(denomination), CoinageItems.coin(denomination).getDefaultMaxStackSize());
            PurseStorage.ExtractionResult extracted = PurseStorage.extract(purse, denomination, count,
                CoinageServerConfig.capacity(denomination));
            if (extracted.extracted() <= 0) return true;
            ItemStack remainder = target.apply(new ItemStack(CoinageItems.coin(denomination), (int) extracted.extracted()));
            if (!remainder.isEmpty()) {
                PurseStorage.deposit(purse, denomination, remainder.getCount(), CoinageServerConfig.capacity(denomination));
            }
            player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 1.0F);
            return true;
        }
        return true;
    }

    private static void playInsert(Player player) {
        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 1.0F);
    }
}
