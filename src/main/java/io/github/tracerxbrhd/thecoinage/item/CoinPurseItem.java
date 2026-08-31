package io.github.tracerxbrhd.thecoinage.item;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyMath;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.config.CoinageServerConfig;
import io.github.tracerxbrhd.thecoinage.menu.PurseMenu;
import io.github.tracerxbrhd.thecoinage.purse.PurseContents;
import io.github.tracerxbrhd.thecoinage.purse.PurseStorage;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class CoinPurseItem extends Item {
    public CoinPurseItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            int purseSlot = hand == InteractionHand.OFF_HAND ? 40 : player.getInventory().getSelectedSlot();
            serverPlayer.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new PurseMenu(containerId, inventory, purseSlot),
                Component.translatable("container.the_coinage.coin_purse")),
                buffer -> buffer.writeVarInt(purseSlot));
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        PurseContents contents = PurseStorage.read(stack);
        long capacity = CoinageServerConfig.PURSE_CAPACITY.getAsLong();
        if (contents.validate(capacity) != PurseContents.PurseValidation.VALID) {
            tooltip.accept(Component.translatable("tooltip.the_coinage.purse.invalid").withStyle(ChatFormatting.RED));
            tooltip.accept(Component.translatable("tooltip.the_coinage.purse.format", contents.formatVersion())
                .withStyle(ChatFormatting.DARK_RED));
            return;
        }
        tooltip.accept(line(Denomination.COPPER, contents.copper()));
        tooltip.accept(line(Denomination.SILVER, contents.silver()));
        tooltip.accept(line(Denomination.GOLD, contents.gold()));
        try {
            tooltip.accept(Component.translatable("tooltip.the_coinage.purse.total",
                CurrencyMath.formatNormalized(contents.breakdown().normalizedValue(CoinageServerConfig.currencyRules())))
                .withStyle(ChatFormatting.GRAY));
        } catch (ArithmeticException exception) {
            tooltip.accept(Component.translatable("tooltip.the_coinage.purse.total_overflow").withStyle(ChatFormatting.RED));
        }
        tooltip.accept(Component.translatable("tooltip.the_coinage.purse.controls").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Component line(Denomination denomination, long value) {
        return Component.translatable("tooltip.the_coinage.purse.line",
            Component.translatable(denomination.translationKey()), CurrencyMath.formatNormalized(value))
            .withStyle(ChatFormatting.GRAY);
    }

}
