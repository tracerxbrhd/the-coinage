package io.github.tracerxbrhd.thecoinage.trade;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyExchangeTable;
import io.github.tracerxbrhd.thecoinage.data.CoinageDataRegistry;
import io.github.tracerxbrhd.thecoinage.data.CoinageTradeDefinition;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

public final class CoinageTrades {
    private CoinageTrades() {}

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        ResourceLocation profession = BuiltInRegistries.VILLAGER_PROFESSION.getKey(event.getType());
        addMatching(CoinageDataRegistry.trades(), profession.toString(), definition ->
            event.getTrades().get(definition.level()).add(CoinageTradeListings.villager(definition)));
    }

    @SubscribeEvent
    public static void onWandererTrades(WandererTradesEvent event) {
        addMatching(CoinageDataRegistry.trades(), "wandering_generic", definition ->
            event.getGenericTrades().add(CoinageTradeListings.wandering(definition)));
        addMatching(CoinageDataRegistry.trades(), "wandering_rare", definition ->
            event.getRareTrades().add(CoinageTradeListings.wandering(definition)));

        var exchangeShape = CurrencyExchangeTable.forRules(
            io.github.tracerxbrhd.thecoinage.api.currency.CurrencyRules.DEFAULT);
        for (int index = 0; index < exchangeShape.size(); index++) {
            VillagerTrades.ItemListing listing = CoinageTradeListings.exchange(index);
            if (exchangeShape.get(index).rare()) {
                event.getRareTrades().add(listing);
            } else {
                event.getGenericTrades().add(listing);
            }
        }
    }

    private static void addMatching(java.util.List<CoinageTradeDefinition> definitions, String target,
                                    Consumer<CoinageTradeDefinition> adder) {
        for (CoinageTradeDefinition definition : definitions) {
            if (!definition.target().equals(target)) continue;
            for (int i = 0; i < definition.weight(); i++) adder.accept(definition);
        }
    }
}
