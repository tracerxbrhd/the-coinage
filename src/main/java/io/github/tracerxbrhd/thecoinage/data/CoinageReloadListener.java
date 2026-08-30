package io.github.tracerxbrhd.thecoinage.data;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

public final class CoinageReloadListener {
    private CoinageReloadListener() {}

    @SubscribeEvent
    public static void addListener(AddReloadListenerEvent event) {
        event.addListener((ResourceManagerReloadListener) CoinageDataRegistry::reload);
    }
}
