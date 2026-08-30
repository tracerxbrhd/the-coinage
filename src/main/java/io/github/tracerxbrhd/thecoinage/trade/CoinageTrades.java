package io.github.tracerxbrhd.thecoinage.trade;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyAmount;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyRules;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.config.CoinageServerConfig;
import io.github.tracerxbrhd.thecoinage.data.CoinageDataRegistry;
import io.github.tracerxbrhd.thecoinage.data.CoinageTradeDefinition;
import java.util.List;
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
        if (!CoinageServerConfig.VILLAGER_TRADES.get()) return;
        ResourceLocation profession = BuiltInRegistries.VILLAGER_PROFESSION.getKey(event.getType());
        addMatching(CoinageDataRegistry.trades(), profession.toString(), (definition, listing) ->
            event.getTrades().get(definition.level()).add(listing));
    }

    @SubscribeEvent
    public static void onWandererTrades(WandererTradesEvent event) {
        if (!CoinageServerConfig.WANDERING_TRADER_TRADES.get()) return;
        addMatching(CoinageDataRegistry.trades(), "wandering_generic", (definition, listing) ->
            event.getGenericTrades().add(listing));
        addMatching(CoinageDataRegistry.trades(), "wandering_rare", (definition, listing) ->
            event.getRareTrades().add(listing));

        CurrencyRules rules = CoinageServerConfig.currencyRules();
        addExchange(event.getGenericTrades(), new CurrencyAmount(Denomination.COPPER, rules.copperPerSilver()),
            new CurrencyAmount(Denomination.SILVER, 1));
        addExchange(event.getGenericTrades(), new CurrencyAmount(Denomination.SILVER, 1),
            new CurrencyAmount(Denomination.COPPER, rules.copperPerSilver()));
        addExchange(event.getGenericTrades(), new CurrencyAmount(Denomination.SILVER, rules.silverPerGold()),
            new CurrencyAmount(Denomination.GOLD, 1));
        addExchange(event.getRareTrades(), new CurrencyAmount(Denomination.GOLD, 1),
            new CurrencyAmount(Denomination.SILVER, rules.silverPerGold()));
    }

    private static void addExchange(List<VillagerTrades.ItemListing> trades, CurrencyAmount cost,
                                    CurrencyAmount reward) {
        CoinageTradeDefinition definition = new CoinageTradeDefinition(
            ResourceLocation.fromNamespaceAndPath("the_coinage", "generated_exchange"), "wandering_generic", 1, 1,
            cost, null, 0, null, 0, reward, 12, 1, 0);
        trades.add((entity, random) -> definition.createOffer());
    }

    private static void addMatching(List<CoinageTradeDefinition> definitions, String target, TradeAdder adder) {
        for (CoinageTradeDefinition definition : definitions) {
            if (!definition.target().equals(target)) continue;
            VillagerTrades.ItemListing listing = (entity, random) -> definition.createOffer();
            for (int i = 0; i < definition.weight(); i++) adder.add(definition, listing);
        }
    }

    @FunctionalInterface
    private interface TradeAdder {
        void add(CoinageTradeDefinition definition, VillagerTrades.ItemListing listing);
    }
}
