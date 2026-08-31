package io.github.tracerxbrhd.thecoinage.trade;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyExchangeTable;
import io.github.tracerxbrhd.thecoinage.config.CoinageServerConfig;
import io.github.tracerxbrhd.thecoinage.data.CoinageTradeDefinition;
import java.util.function.BooleanSupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;

/**
 * Builds listings without touching SERVER config during TagsUpdatedEvent.
 * Config is evaluated only when a server-side merchant generates an offer.
 */
public final class CoinageTradeListings {
    private CoinageTradeListings() {}

    public static VillagerTrades.ItemListing villager(CoinageTradeDefinition definition) {
        return gated(() -> CoinageServerConfig.VILLAGER_TRADES.get(),
            (entity, random) -> definition.createOffer());
    }

    public static VillagerTrades.ItemListing wandering(CoinageTradeDefinition definition) {
        return gated(() -> CoinageServerConfig.WANDERING_TRADER_TRADES.get(),
            (entity, random) -> definition.createOffer());
    }

    public static VillagerTrades.ItemListing exchange(int index) {
        return gated(() -> CoinageServerConfig.WANDERING_TRADER_TRADES.get(), (entity, random) -> {
            CurrencyExchangeTable.Exchange exchange = CurrencyExchangeTable
                .forRules(CoinageServerConfig.currencyRules()).get(index);
            return CoinageTradeDefinition.exchange(
                ResourceLocation.fromNamespaceAndPath("the_coinage", "generated_exchange_" + index),
                exchange.cost(), exchange.reward()).createOffer();
        });
    }

    static VillagerTrades.ItemListing gated(BooleanSupplier enabled, VillagerTrades.ItemListing delegate) {
        return (entity, random) -> enabled.getAsBoolean() ? delegate.getOffer(entity, random) : null;
    }
}
