package io.github.tracerxbrhd.thecoinage.trade;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyAmount;
import io.github.tracerxbrhd.thecoinage.registry.CoinageDataComponents;
import io.github.tracerxbrhd.thecoinage.registry.CoinageItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

/** Builds config-sensitive exchange offers without relying on a fixed datapack ratio. */
public final class CoinageExchangeTrades {
    private CoinageExchangeTrades() {}

    public static MerchantOffer create(CurrencyAmount cost, CurrencyAmount reward) {
        Item costItem = CoinageItems.coin(cost.denomination());
        if (cost.count() > costItem.getDefaultMaxStackSize()) {
            throw new IllegalArgumentException("currency cost does not fit in one merchant slot: " + cost);
        }
        ItemCost itemCost = new ItemCost(costItem, (int) cost.count()).withComponents(builder ->
            builder.expect(CoinageDataComponents.MERCHANT_PRICE.get(), cost));

        Item rewardItem = CoinageItems.coin(reward.denomination());
        int visualReward = (int) Math.min(reward.count(), rewardItem.getDefaultMaxStackSize());
        ItemStack result = new ItemStack(rewardItem, visualReward);
        result.set(CoinageDataComponents.MERCHANT_REWARD.get(), reward);
        return new MerchantOffer(itemCost, result, 12, 1, 0.0F);
    }
}
