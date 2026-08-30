package io.github.tracerxbrhd.thecoinage.mixin;

import io.github.tracerxbrhd.thecoinage.api.CoinageApi;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyAmount;
import io.github.tracerxbrhd.thecoinage.trade.MerchantCurrencySupport;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.inventory.MerchantContainer.class)
public abstract class MerchantContainerMixin {
    @Shadow @Final private Merchant merchant;
    @Shadow @Final private NonNullList<ItemStack> itemStacks;
    @Shadow private MerchantOffer activeOffer;
    @Shadow private int selectionHint;
    @Shadow private int futureXp;

    @Inject(method = "updateSellItem", at = @At("TAIL"))
    private void theCoinage$resolvePurseOffer(CallbackInfo callback) {
        if (!(merchant.getTradingPlayer() instanceof ServerPlayer player)) return;
        var offers = merchant.getOffers();
        if (selectionHint < 0 || selectionHint >= offers.size()) return;
        MerchantOffer offer = offers.get(selectionHint);
        CurrencyAmount price = MerchantCurrencySupport.price(offer);
        if (price == null || offer.isOutOfStock()) return;
        boolean affordable = CoinageApi.find().map(api ->
            api.canAfford(player, MerchantCurrencySupport.breakdown(price))).orElse(false);
        if (affordable) {
            activeOffer = offer;
            itemStacks.set(2, offer.assemble());
            futureXp = offer.getXp();
        } else {
            activeOffer = null;
            itemStacks.set(2, ItemStack.EMPTY);
            futureXp = 0;
        }
        merchant.notifyTradeUpdated(itemStacks.get(2));
    }
}
