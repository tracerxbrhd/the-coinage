package io.github.tracerxbrhd.thecoinage.mixin;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyAmount;
import io.github.tracerxbrhd.thecoinage.registry.CoinageDataComponents;
import io.github.tracerxbrhd.thecoinage.registry.CoinageItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The merchant-price component describes the offer; players should insert ordinary coin stacks. */
@Mixin(ItemCost.class)
public abstract class ItemCostMixin {
    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void theCoinage$acceptUnmarkedCoins(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        ItemCost cost = (ItemCost) (Object) this;
        CurrencyAmount price = cost.itemStack().get(CoinageDataComponents.MERCHANT_PRICE.get());
        if (price != null) callback.setReturnValue(CoinageItems.denomination(stack) == price.denomination());
    }
}
