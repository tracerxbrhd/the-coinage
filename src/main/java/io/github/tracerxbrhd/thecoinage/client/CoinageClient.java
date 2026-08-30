package io.github.tracerxbrhd.thecoinage.client;

import io.github.tracerxbrhd.thecoinage.registry.CoinageMenus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = "the_coinage", value = Dist.CLIENT)
public final class CoinageClient {
    private CoinageClient() {}

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CoinageMenus.PURSE.get(), PurseScreen::new);
    }
}
