package io.github.tracerxbrhd.thecoinage.data;

import com.google.gson.JsonObject;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyAmount;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.registry.CoinageDataComponents;
import io.github.tracerxbrhd.thecoinage.registry.CoinageItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

/** Reloadable, deliberately small merchant offer schema. */
public record CoinageTradeDefinition(ResourceLocation id, String target, int level, int weight,
                                     CurrencyAmount currencyCost, ResourceLocation itemCost, int itemCostCount,
                                     ResourceLocation resultItem, int resultCount, CurrencyAmount currencyReward,
                                     int maxUses, int xp, float priceMultiplier) {
    public static CoinageTradeDefinition exchange(ResourceLocation id, CurrencyAmount cost, CurrencyAmount reward) {
        return new CoinageTradeDefinition(id, "wandering_generic", 1, 1, cost, null, 0,
            null, 0, reward, 12, 1, 0.0F);
    }

    public static CoinageTradeDefinition parse(ResourceLocation id, JsonObject json) {
        int format = GsonHelper.getAsInt(json, "format_version", 1);
        if (format != 1) throw new IllegalArgumentException("unsupported format_version " + format);
        String target = requiredString(json, "target");
        int level = range(GsonHelper.getAsInt(json, "level", 1), 1, 5, "level");
        int weight = range(GsonHelper.getAsInt(json, "weight", 1), 1, 64, "weight");

        JsonObject cost = GsonHelper.getAsJsonObject(json, "cost");
        String costType = GsonHelper.getAsString(cost, "type");
        CurrencyAmount currencyCost = null;
        ResourceLocation itemCost = null;
        int itemCostCount = 0;
        if ("currency".equals(costType)) {
            currencyCost = currency(cost);
            if (currencyCost.count() < 1) throw new IllegalArgumentException("currency cost must be positive");
            int maximum = CoinageItems.coin(currencyCost.denomination()).getDefaultMaxStackSize();
            if (currencyCost.count() > maximum) {
                throw new IllegalArgumentException("currency cost must fit in one merchant slot (max " + maximum + ")");
            }
        } else if ("item".equals(costType)) {
            itemCost = ResourceLocation.parse(requiredString(cost, "item"));
            itemCostCount = range(GsonHelper.getAsInt(cost, "count", 1), 1, 64, "item cost count");
        } else {
            throw new IllegalArgumentException("cost.type must be 'currency' or 'item'");
        }

        JsonObject result = GsonHelper.getAsJsonObject(json, "result");
        String resultType = GsonHelper.getAsString(result, "type");
        ResourceLocation resultItem = null;
        int resultCount = 0;
        CurrencyAmount currencyReward = null;
        if ("currency".equals(resultType)) {
            currencyReward = currency(result);
            if (currencyReward.count() < 1) throw new IllegalArgumentException("currency reward must be positive");
        } else if ("item".equals(resultType)) {
            resultItem = ResourceLocation.parse(requiredString(result, "item"));
            resultCount = range(GsonHelper.getAsInt(result, "count", 1), 1, 64, "result count");
        } else {
            throw new IllegalArgumentException("result.type must be 'currency' or 'item'");
        }

        int maxUses = range(GsonHelper.getAsInt(json, "max_uses", 12), 1, 10_000, "max_uses");
        int xp = range(GsonHelper.getAsInt(json, "xp", 1), 0, 100_000, "xp");
        float multiplier = GsonHelper.getAsFloat(json, "price_multiplier", 0.05F);
        if (!Float.isFinite(multiplier) || multiplier < 0) throw new IllegalArgumentException("invalid price_multiplier");
        return new CoinageTradeDefinition(id, target, level, weight, currencyCost, itemCost, itemCostCount,
            resultItem, resultCount, currencyReward, maxUses, xp, multiplier);
    }

    public MerchantOffer createOffer() {
        ItemCost cost;
        if (currencyCost != null) {
            Item item = CoinageItems.coin(currencyCost.denomination());
            if (currencyCost.count() > item.getDefaultMaxStackSize()) {
                throw new IllegalStateException("currency cost does not fit in one merchant slot: " + currencyCost);
            }
            cost = new ItemCost(item, (int) currencyCost.count()).withComponents(builder ->
                builder.expect(CoinageDataComponents.MERCHANT_PRICE.get(), currencyCost));
        } else {
            Item item = BuiltInRegistries.ITEM.get(itemCost);
            if (item == null || item == net.minecraft.world.item.Items.AIR) return null;
            cost = new ItemCost(item, itemCostCount);
        }

        ItemStack result;
        if (currencyReward != null) {
            Item item = CoinageItems.coin(currencyReward.denomination());
            result = new ItemStack(item, (int) Math.min(currencyReward.count(), item.getDefaultMaxStackSize()));
            result.set(CoinageDataComponents.MERCHANT_REWARD.get(), currencyReward);
        } else {
            Item item = BuiltInRegistries.ITEM.get(resultItem);
            if (item == null || item == net.minecraft.world.item.Items.AIR) return null;
            result = new ItemStack(item, resultCount);
        }
        return new MerchantOffer(cost, result, maxUses, xp, priceMultiplier);
    }

    private static CurrencyAmount currency(JsonObject json) {
        Denomination denomination = Denomination.byName(requiredString(json, "denomination"))
            .orElseThrow(() -> new IllegalArgumentException("unknown denomination"));
        long count = GsonHelper.getAsLong(json, "count");
        return new CurrencyAmount(denomination, count);
    }

    private static String requiredString(JsonObject json, String name) {
        String value = GsonHelper.getAsString(json, name);
        if (value.isBlank()) throw new IllegalArgumentException(name + " cannot be blank");
        return value;
    }

    private static int range(int value, int min, int max, String name) {
        if (value < min || value > max) throw new IllegalArgumentException(name + " must be in " + min + ".." + max);
        return value;
    }
}
