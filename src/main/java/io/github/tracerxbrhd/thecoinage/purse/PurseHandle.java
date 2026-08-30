package io.github.tracerxbrhd.thecoinage.purse;

import net.minecraft.world.item.ItemStack;

/** Internal mutable location of an inventory or accessory purse. */
public interface PurseHandle {
    ItemStack get();
    boolean set(ItemStack stack);
    boolean isValid();
    String source();
}
