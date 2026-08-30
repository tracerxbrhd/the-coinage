package io.github.tracerxbrhd.thecoinage.item;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyAmount;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.registry.CoinageDataComponents;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class CoinItem extends Item {
    private final Denomination denomination;

    public CoinItem(Denomination denomination, Properties properties) {
        super(properties);
        this.denomination = denomination;
    }

    public Denomination denomination() {
        return denomination;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CurrencyAmount merchantPrice = stack.get(CoinageDataComponents.MERCHANT_PRICE.get());
        if (merchantPrice != null) {
            tooltip.add(Component.translatable("tooltip.the_coinage.merchant_price", merchantPrice.count(),
                Component.translatable(merchantPrice.denomination().translationKey())).withStyle(ChatFormatting.GOLD));
        }
    }
}
