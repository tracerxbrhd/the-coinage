package io.github.tracerxbrhd.thecoinage.api;

import io.github.tracerxbrhd.thecoinage.TheCoinage;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyBreakdown;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyRules;
import io.github.tracerxbrhd.thecoinage.config.CoinageServerConfig;
import io.github.tracerxbrhd.thecoinage.payment.PlayerFundsService;
import io.github.tracerxbrhd.thecoinage.purse.ActivePurseResolver;
import io.github.tracerxbrhd.thecoinage.reward.CurrencyRewardService;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class DefaultCoinageApi implements CoinageApi {
    @Override public ResourceLocation serviceId() { return TheCoinage.id("currency"); }
    @Override public CurrencyRules currencyRules() { return CoinageServerConfig.currencyRules(); }
    @Override public Optional<ItemStack> findActivePurse(ServerPlayer player) {
        return ActivePurseResolver.find(player).map(handle -> handle.get().copy());
    }
    @Override public CurrencyBreakdown getAvailableCurrency(ServerPlayer player) {
        return PlayerFundsService.available(player);
    }
    @Override public boolean canAfford(ServerPlayer player, CurrencyBreakdown price) {
        return simulatePayment(player, price).successful();
    }
    @Override public PaymentResult simulatePayment(ServerPlayer player, CurrencyBreakdown price) {
        return PlayerFundsService.simulate(player, price);
    }
    @Override public PaymentResult pay(ServerPlayer player, CurrencyBreakdown price) {
        return PlayerFundsService.pay(player, price);
    }
    @Override public CurrencyBreakdown give(ServerPlayer player, CurrencyBreakdown reward) {
        return CurrencyRewardService.give(player, reward);
    }
}
