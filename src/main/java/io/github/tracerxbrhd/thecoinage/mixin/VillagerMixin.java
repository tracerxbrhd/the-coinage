package io.github.tracerxbrhd.thecoinage.mixin;

import io.github.tracerxbrhd.thecoinage.config.CoinageServerConfig;
import io.github.tracerxbrhd.thecoinage.trade.MerchantCurrencySupport;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin {
    @Inject(method = "updateTrades", at = @At("TAIL"))
    private void theCoinage$applyTradeConfig(ServerLevel level, CallbackInfo callback) {
        if (!CoinageServerConfig.VILLAGER_TRADES.get()) {
            ((Villager) (Object) this).getOffers().removeIf(MerchantCurrencySupport::isCurrencyOffer);
        }
    }
}
