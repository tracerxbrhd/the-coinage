package io.github.tracerxbrhd.thecoinage.data;

import com.google.gson.JsonParser;
import io.github.tracerxbrhd.thecoinage.TheCoinage;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/** Immutable snapshots swapped only after a complete datapack scan. */
public final class CoinageDataRegistry {
    private static volatile List<CoinageTradeDefinition> trades = List.of();
    private static volatile List<MobDropDefinition> mobDrops = List.of();

    private CoinageDataRegistry() {}

    public static void reload(ResourceManager manager) {
        trades = load(manager, "the_coinage/trades", CoinageTradeDefinition::parse);
        mobDrops = load(manager, "the_coinage/mob_drops", MobDropDefinition::parse);
        TheCoinage.LOGGER.info("Loaded {} Coinage trades and {} mob drop definitions", trades.size(), mobDrops.size());
    }

    public static List<CoinageTradeDefinition> trades() { return trades; }
    public static List<MobDropDefinition> mobDrops() { return mobDrops; }

    private static <T> List<T> load(ResourceManager manager, String root, Parser<T> parser) {
        List<T> loaded = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Resource> entry : manager.listResources(root,
            id -> id.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation file = entry.getKey();
            String path = file.getPath().substring(root.length() + 1, file.getPath().length() - 5);
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(file.getNamespace(), path);
            try (Reader reader = entry.getValue().openAsReader()) {
                loaded.add(parser.parse(id, JsonParser.parseReader(reader).getAsJsonObject()));
            } catch (Exception exception) {
                TheCoinage.LOGGER.error("Skipping invalid Coinage data file {}: {}", file, rootCause(exception));
            }
        }
        loaded.sort(Comparator.comparing(Object::toString));
        return List.copyOf(loaded);
    }

    private static String rootCause(Throwable throwable) {
        while (throwable.getCause() != null) throwable = throwable.getCause();
        return throwable.getMessage() == null ? throwable.getClass().getSimpleName() : throwable.getMessage();
    }

    @FunctionalInterface
    private interface Parser<T> { T parse(ResourceLocation id, com.google.gson.JsonObject json); }
}
