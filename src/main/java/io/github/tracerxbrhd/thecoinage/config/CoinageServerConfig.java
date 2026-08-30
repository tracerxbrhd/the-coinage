package io.github.tracerxbrhd.thecoinage.config;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyRules;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CoinageServerConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.LongValue COPPER_PER_SILVER;
    public static final ModConfigSpec.LongValue SILVER_PER_GOLD;
    public static final ModConfigSpec.LongValue PURSE_CAPACITY;
    public static final ModConfigSpec.BooleanValue AUTOMATIC_PURSE_CONVERSION;
    public static final ModConfigSpec.BooleanValue AUTOMATIC_COIN_PICKUP;
    public static final ModConfigSpec.BooleanValue MOB_COIN_DROPS;
    public static final ModConfigSpec.BooleanValue VILLAGER_TRADES;
    public static final ModConfigSpec.BooleanValue WANDERING_TRADER_TRADES;
    public static final ModConfigSpec.DoubleValue LOOT_FREQUENCY_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Authoritative denomination hierarchy.").push("currency");
        COPPER_PER_SILVER = builder.comment("Copper Coins represented by one Silver Coin.")
            .defineInRange("copperPerSilver", 100L, 2L, 1_000_000L);
        SILVER_PER_GOLD = builder.comment("Silver Coins represented by one Gold Coin.")
            .defineInRange("silverPerGold", 100L, 2L, 1_000_000L);
        AUTOMATIC_PURSE_CONVERSION = builder.comment(
            "Allow payments to break and normalize purse denominations. Disabled by default.")
            .define("automaticPurseConversion", false);
        builder.pop();

        builder.comment("Coin Purse behavior.").push("purse");
        PURSE_CAPACITY = builder.comment("Maximum stored count per denomination.")
            .defineInRange("capacityPerDenomination", 1_000_000L, 1L, 1_000_000_000_000L);
        AUTOMATIC_COIN_PICKUP = builder.comment("Deposit picked-up Coinage coins into the active purse first.")
            .define("automaticCoinPickup", true);
        builder.pop();

        builder.comment("World and merchant integration.").push("gameplay");
        MOB_COIN_DROPS = builder.comment("Enable datapack-defined mob coin drops. Disabled by default.")
            .define("mobCoinDrops", false);
        VILLAGER_TRADES = builder.define("villagerTrades", true);
        WANDERING_TRADER_TRADES = builder.define("wanderingTraderTrades", true);
        LOOT_FREQUENCY_MULTIPLIER = builder.comment("Reserved multiplier for Coinage loot rolls; 0 disables additions.")
            .defineInRange("lootFrequencyMultiplier", 1.0D, 0.0D, 10.0D);
        builder.pop();

        DEBUG_LOGGING = builder.comment("Enable detailed diagnostics without transaction spam.")
            .define("debugLogging", false);
        SPEC = builder.build();
    }

    private CoinageServerConfig() {}

    public static CurrencyRules currencyRules() {
        return new CurrencyRules(COPPER_PER_SILVER.getAsLong(), SILVER_PER_GOLD.getAsLong());
    }

    public static long capacity(Denomination denomination) {
        return PURSE_CAPACITY.getAsLong();
    }
}
