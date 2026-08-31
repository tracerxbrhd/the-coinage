package io.github.tracerxbrhd.thecoinage.mixin;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyAmount;
import io.github.tracerxbrhd.thecoinage.reward.CurrencyRewardService;
import io.github.tracerxbrhd.thecoinage.trade.MerchantCurrencySupport;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.inventory.MerchantResultSlot.class)
public abstract class MerchantResultSlotMixin {
    @Inject(method = "onTake", at = @At("HEAD"))
    private void theCoinage$deliverCurrencyReward(Player player, ItemStack resultStack, CallbackInfo callback) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        CurrencyAmount reward = MerchantCurrencySupport.reward(resultStack);
        if (reward == null) return;
        CurrencyRewardService.give(serverPlayer, MerchantCurrencySupport.breakdown(reward));
        resultStack.setCount(0);
    }
}
