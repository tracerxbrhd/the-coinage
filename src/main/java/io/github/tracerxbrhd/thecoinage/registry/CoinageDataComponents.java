package io.github.tracerxbrhd.thecoinage.registry;

import io.github.tracerxbrhd.thecoinage.TheCoinage;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyAmount;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.purse.PurseContents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CoinageDataComponents {
    public static final DeferredRegister.DataComponents TYPES =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TheCoinage.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PurseContents>> PURSE_CONTENTS =
        TYPES.registerComponentType("purse_contents", builder -> builder
            .persistent(PurseContents.CODEC)
            .networkSynchronized(PurseContents.STREAM_CODEC)
            .cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CurrencyAmount>> MERCHANT_PRICE =
        TYPES.registerComponentType("merchant_price", builder -> builder
            .persistent(CurrencyAmount.CODEC)
            .networkSynchronized(StreamCodec.of(CoinageDataComponents::encodeAmount, CoinageDataComponents::decodeAmount))
            .cacheEncoding());

    /** Full reward behind a merchant result, including amounts larger than an ItemStack. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CurrencyAmount>> MERCHANT_REWARD =
        TYPES.registerComponentType("merchant_reward", builder -> builder
            .persistent(CurrencyAmount.CODEC)
            .networkSynchronized(StreamCodec.of(CoinageDataComponents::encodeAmount, CoinageDataComponents::decodeAmount))
            .cacheEncoding());

    private CoinageDataComponents() {}

    private static void encodeAmount(RegistryFriendlyByteBuf buffer, CurrencyAmount amount) {
        buffer.writeEnum(amount.denomination());
        buffer.writeVarLong(amount.count());
    }

    private static CurrencyAmount decodeAmount(RegistryFriendlyByteBuf buffer) {
        return new CurrencyAmount(buffer.readEnum(Denomination.class), buffer.readVarLong());
    }
}
