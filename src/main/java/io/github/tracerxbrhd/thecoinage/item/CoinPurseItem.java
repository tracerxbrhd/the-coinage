package io.github.tracerxbrhd.thecoinage.item;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyMath;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.config.CoinageServerConfig;
import io.github.tracerxbrhd.thecoinage.menu.PurseMenu;
import io.github.tracerxbrhd.thecoinage.purse.PurseContents;
import io.github.tracerxbrhd.thecoinage.purse.PurseStorage;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class CoinPurseItem extends Item {
    public CoinPurseItem(Properties properties) {
        super(properties);
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

}
