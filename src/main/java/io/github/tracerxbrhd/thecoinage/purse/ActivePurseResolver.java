package io.github.tracerxbrhd.thecoinage.purse;

import io.github.tracerxbrhd.thecoinage.compat.AccessoryPurseAdapter;
import io.github.tracerxbrhd.thecoinage.registry.CoinageItems;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Deterministic accessory-first, inventory-slot-order purse resolution. */
public final class ActivePurseResolver {
    private ActivePurseResolver() {}

    public static Optional<PurseHandle> find(Player player) {
        Optional<PurseHandle> equipped = AccessoryPurseAdapter.find(player);
        if (equipped.isPresent()) return equipped;
        for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
            if (player.getInventory().getItem(index).is(CoinageItems.COIN_PURSE.get())) {
                return Optional.of(new InventoryHandle(player, index));
            }
        }
        return Optional.empty();
    }

    private record InventoryHandle(Player player, int index) implements PurseHandle {
        @Override public ItemStack get() { return player.getInventory().getItem(index); }
        @Override public boolean set(ItemStack stack) {
            if (!isValid()) return false;
            player.getInventory().setItem(index, stack);
            player.getInventory().setChanged();
            return true;
        }
        @Override public boolean isValid() { return get().is(CoinageItems.COIN_PURSE.get()); }
        @Override public String source() { return "inventory:" + index; }
    }
}
