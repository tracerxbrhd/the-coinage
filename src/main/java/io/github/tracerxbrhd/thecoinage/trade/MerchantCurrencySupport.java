package io.github.tracerxbrhd.thecoinage.trade;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyAmount;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyBreakdown;
import io.github.tracerxbrhd.thecoinage.registry.CoinageDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public final class MerchantCurrencySupport {
    private MerchantCurrencySupport() {}

    public static CurrencyAmount price(MerchantOffer offer) {
        return offer == null ? null : offer.getItemCostA().itemStack().get(CoinageDataComponents.MERCHANT_PRICE.get());
    }

    public static CurrencyAmount reward(ItemStack result) {
        return result.get(CoinageDataComponents.MERCHANT_REWARD.get());
    }

    public static boolean isCurrencyOffer(MerchantOffer offer) {
        return price(offer) != null || reward(offer.getResult()) != null;
    }

    public static CurrencyBreakdown breakdown(CurrencyAmount amount) {
        return CurrencyBreakdown.ZERO.with(amount.denomination(), amount.count());
    }
}
