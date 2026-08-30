package io.github.tracerxbrhd.thecoinage.mixin;

import io.github.tracerxbrhd.thecoinage.api.CoinageApi;
import io.github.tracerxbrhd.thecoinage.api.PaymentResult;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyAmount;
import io.github.tracerxbrhd.thecoinage.reward.CurrencyRewardService;
import io.github.tracerxbrhd.thecoinage.trade.MerchantCurrencySupport;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.inventory.MerchantResultSlot.class)
public abstract class MerchantResultSlotMixin {
    @Shadow @Final private MerchantContainer slots;
    @Shadow @Final private Player player;
    @Shadow @Final private Merchant merchant;

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void theCoinage$takeCurrencyOffer(Player takingPlayer, ItemStack resultStack, CallbackInfo callback) {
        if (!(takingPlayer instanceof ServerPlayer serverPlayer)) return;
        MerchantOffer offer = slots.getActiveOffer();
        if (offer == null) return;
        CurrencyAmount price = MerchantCurrencySupport.price(offer);
        CurrencyAmount reward = MerchantCurrencySupport.reward(resultStack);

        if (price == null) {
            if (reward != null) {
                CurrencyRewardService.give(serverPlayer, MerchantCurrencySupport.breakdown(reward));
                resultStack.setCount(0);
            }
            return;
        }

        PaymentResult payment = CoinageApi.find().map(api ->
            api.pay(serverPlayer, MerchantCurrencySupport.breakdown(price))).orElse(null);
        if (payment == null || !payment.successful()) {
            resultStack.setCount(0);
            slots.setChanged();
            callback.cancel();
            return;
        }
        if (reward != null) {
            CurrencyRewardService.give(serverPlayer, MerchantCurrencySupport.breakdown(reward));
            resultStack.setCount(0);
        }
        merchant.notifyTrade(offer);
        serverPlayer.awardStat(Stats.TRADED_WITH_VILLAGER);
        merchant.overrideXp(merchant.getVillagerXp() + offer.getXp());
        slots.setItem(0, slots.getItem(0));
        slots.setItem(1, slots.getItem(1));
        slots.setChanged();
        callback.cancel();
    }
}
