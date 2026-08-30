package io.github.tracerxbrhd.thecoinage.registry;

import io.github.tracerxbrhd.thecoinage.TheCoinage;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.item.CoinItem;
import io.github.tracerxbrhd.thecoinage.item.CoinPurseItem;
import io.github.tracerxbrhd.thecoinage.purse.PurseContents;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CoinageItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheCoinage.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
        TheCoinage.MOD_ID);

    public static final DeferredItem<Item> COPPER_COIN = ITEMS.registerItem("copper_coin",
        properties -> new CoinItem(Denomination.COPPER, properties));
    public static final DeferredItem<Item> SILVER_COIN = ITEMS.registerItem("silver_coin",
        properties -> new CoinItem(Denomination.SILVER, properties));
    public static final DeferredItem<Item> GOLD_COIN = ITEMS.registerItem("gold_coin",
        properties -> new CoinItem(Denomination.GOLD, properties));
    public static final DeferredItem<Item> COIN_PURSE = ITEMS.registerItem("coin_purse",
        properties -> new CoinPurseItem(properties.stacksTo(1)
            .component(CoinageDataComponents.PURSE_CONTENTS.get(), PurseContents.EMPTY)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("the_coinage",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.the_coinage"))
            .icon(() -> new ItemStack(COIN_PURSE.get()))
            .displayItems((parameters, output) -> {
                output.accept(COPPER_COIN.get());
                output.accept(SILVER_COIN.get());
                output.accept(GOLD_COIN.get());
                output.accept(COIN_PURSE.get());
            }).build());

    private CoinageItems() {}

    public static Item coin(Denomination denomination) {
        return switch (denomination) {
            case COPPER -> COPPER_COIN.get();
            case SILVER -> SILVER_COIN.get();
            case GOLD -> GOLD_COIN.get();
        };
    }

    public static Denomination denomination(ItemStack stack) {
        return stack.getItem() instanceof CoinItem coin ? coin.denomination() : null;
    }

    public static Map<Denomination, Item> coinMap() {
        EnumMap<Denomination, Item> result = new EnumMap<>(Denomination.class);
        for (Denomination denomination : Denomination.values()) result.put(denomination, coin(denomination));
        return Map.copyOf(result);
    }
}
