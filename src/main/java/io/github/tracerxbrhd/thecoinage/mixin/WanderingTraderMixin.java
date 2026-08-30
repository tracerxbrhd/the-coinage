package io.github.tracerxbrhd.thecoinage.mixin;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyAmount;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyRules;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.config.CoinageServerConfig;
import io.github.tracerxbrhd.thecoinage.trade.CoinageExchangeTrades;
import io.github.tracerxbrhd.thecoinage.trade.MerchantCurrencySupport;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin {
    @Inject(method = "updateTrades", at = @At("TAIL"))
    private void theCoinage$addCurrencyTrades(ServerLevel level, CallbackInfo callback) {
        MerchantOffers offers = ((WanderingTrader) (Object) this).getOffers();
        if (!CoinageServerConfig.WANDERING_TRADER_TRADES.get()) {
            offers.removeIf(MerchantCurrencySupport::isCurrencyOffer);
            return;
        }

        CurrencyRules rules = CoinageServerConfig.currencyRules();
        offers.add(CoinageExchangeTrades.create(
            new CurrencyAmount(Denomination.COPPER, rules.copperPerSilver()),
            new CurrencyAmount(Denomination.SILVER, 1)));
        offers.add(CoinageExchangeTrades.create(
            new CurrencyAmount(Denomination.SILVER, 1),
            new CurrencyAmount(Denomination.COPPER, rules.copperPerSilver())));
        offers.add(CoinageExchangeTrades.create(
            new CurrencyAmount(Denomination.SILVER, rules.silverPerGold()),
            new CurrencyAmount(Denomination.GOLD, 1)));
        offers.add(CoinageExchangeTrades.create(
            new CurrencyAmount(Denomination.GOLD, 1),
            new CurrencyAmount(Denomination.SILVER, rules.silverPerGold())));
    }
}
