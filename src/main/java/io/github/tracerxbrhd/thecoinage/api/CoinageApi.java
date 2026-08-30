package io.github.tracerxbrhd.thecoinage.api;

import dev.uapi.api.services.UApiService;
import dev.uapi.api.services.UApiServices;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyBreakdown;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyRules;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Stable server-side entry point for currency, purse, reward and payment integration. */
public interface CoinageApi extends UApiService {
    static Optional<CoinageApi> find() {
        return UApiServices.find(CoinageApi.class);
    }

    CurrencyRules currencyRules();
    Optional<ItemStack> findActivePurse(ServerPlayer player);
    CurrencyBreakdown getAvailableCurrency(ServerPlayer player);
    boolean canAfford(ServerPlayer player, CurrencyBreakdown price);
    PaymentResult simulatePayment(ServerPlayer player, CurrencyBreakdown price);
    PaymentResult pay(ServerPlayer player, CurrencyBreakdown price);
    CurrencyBreakdown give(ServerPlayer player, CurrencyBreakdown reward);
}
