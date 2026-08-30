package io.github.tracerxbrhd.thecoinage;

import com.mojang.logging.LogUtils;
import dev.uapi.api.services.ServiceRegistration;
import dev.uapi.api.services.ServiceScope;
import dev.uapi.api.services.UApiServices;
import dev.uapi.reward.RewardRegistry;
import io.github.tracerxbrhd.thecoinage.api.CoinageApi;
import io.github.tracerxbrhd.thecoinage.api.DefaultCoinageApi;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyBreakdown;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.config.CoinageServerConfig;
import io.github.tracerxbrhd.thecoinage.command.CoinageCommands;
import io.github.tracerxbrhd.thecoinage.data.CoinageReloadListener;
import io.github.tracerxbrhd.thecoinage.gameplay.CoinageGameplayEvents;
import io.github.tracerxbrhd.thecoinage.registry.CoinageDataComponents;
import io.github.tracerxbrhd.thecoinage.registry.CoinageItems;
import io.github.tracerxbrhd.thecoinage.registry.CoinageMenus;
import io.github.tracerxbrhd.thecoinage.reward.CurrencyRewardService;
import io.github.tracerxbrhd.thecoinage.trade.CoinageTrades;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(TheCoinage.MOD_ID)
public final class TheCoinage {
    public static final String MOD_ID = "the_coinage";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static ServiceRegistration apiRegistration;

    public TheCoinage(IEventBus modBus, ModContainer container) {
        CoinageDataComponents.TYPES.register(modBus);
        CoinageItems.ITEMS.register(modBus);
        CoinageItems.TABS.register(modBus);
        CoinageMenus.TYPES.register(modBus);
        container.registerConfig(ModConfig.Type.SERVER, CoinageServerConfig.SPEC, "uapi/the-coinage/server.toml");

        NeoForge.EVENT_BUS.register(CoinageCommands.class);
        NeoForge.EVENT_BUS.register(CoinageReloadListener.class);
        NeoForge.EVENT_BUS.register(CoinageGameplayEvents.class);
        NeoForge.EVENT_BUS.register(CoinageTrades.class);

        apiRegistration = UApiServices.register(CoinageApi.class, new DefaultCoinageApi(), ServiceScope.GLOBAL);
        RewardRegistry.registerProvider(id("currency"), (context, data, random) -> {
            Denomination denomination = Denomination.byName(GsonHelper.getAsString(data, "denomination", "copper"))
                .orElse(null);
            if (denomination == null) return false;
            int min = Math.max(0, GsonHelper.getAsInt(data, "min", 1));
            int max = Math.max(min, GsonHelper.getAsInt(data, "max", min));
            long amount = min + random.nextInt(max - min + 1);
            CurrencyRewardService.give(context.player(), CurrencyBreakdown.ZERO.with(denomination, amount));
            return amount > 0;
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
