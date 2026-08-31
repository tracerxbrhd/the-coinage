package io.github.tracerxbrhd.thecoinage.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyExchangeTable;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyRules;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;
import org.junit.jupiter.api.Test;

class CoinageTradeListingsTest {
    @Test void tradeEventsRegisterListingsWithoutReadingServerConfig() {
        var generic = new ArrayList<VillagerTrades.ItemListing>();
        var rare = new ArrayList<VillagerTrades.ItemListing>();

        CoinageTrades.onWandererTrades(new WandererTradesEvent(generic, rare, RegistryAccess.EMPTY));

        assertEquals(3, generic.size());
        assertEquals(1, rare.size());
        assertEquals(4, CurrencyExchangeTable.forRules(CurrencyRules.DEFAULT).size());
    }

    @Test void villagerTradeEventIsAlsoSafeBeforeServerConfigLoads() {
        LoadingModList.of(java.util.List.of(), java.util.List.of(), java.util.List.of(),
            java.util.List.of(), java.util.Map.of());
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var trades = new Int2ObjectOpenHashMap<java.util.List<VillagerTrades.ItemListing>>();
        for (int level = 1; level <= 5; level++) trades.put(level, new ArrayList<>());
        VillagerProfession farmer = BuiltInRegistries.VILLAGER_PROFESSION.get(
            ResourceLocation.withDefaultNamespace("farmer"));

        assertDoesNotThrow(() -> CoinageTrades.onVillagerTrades(
            new VillagerTradesEvent(trades, farmer, RegistryAccess.EMPTY)));
    }

    @Test void toggleIsEvaluatedOnlyWhenAnOfferIsGenerated() {
        AtomicBoolean enabled = new AtomicBoolean(false);
        AtomicInteger toggleReads = new AtomicInteger();
        AtomicInteger delegateCalls = new AtomicInteger();
        VillagerTrades.ItemListing listing = CoinageTradeListings.gated(() -> {
            toggleReads.incrementAndGet();
            return enabled.get();
        }, (entity, random) -> {
            delegateCalls.incrementAndGet();
            return null;
        });

        assertEquals(0, toggleReads.get());
        assertNull(listing.getOffer(null, null));
        assertEquals(1, toggleReads.get());
        assertEquals(0, delegateCalls.get());

        enabled.set(true);
        assertNull(listing.getOffer(null, null));
        assertEquals(2, toggleReads.get());
        assertEquals(1, delegateCalls.get());
    }
}
