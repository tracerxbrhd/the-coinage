package io.github.tracerxbrhd.thecoinage.data;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public final class CoinageReloadListener {
    private CoinageReloadListener() {}

    @SubscribeEvent
    public static void addListener(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath("the_coinage", "data"),
            (ResourceManagerReloadListener) CoinageDataRegistry::reload);
    }
}
