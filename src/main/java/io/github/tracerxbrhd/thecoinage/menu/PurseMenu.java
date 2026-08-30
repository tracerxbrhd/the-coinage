package io.github.tracerxbrhd.thecoinage.menu;

import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.config.CoinageServerConfig;
import io.github.tracerxbrhd.thecoinage.purse.PurseContents;
import io.github.tracerxbrhd.thecoinage.purse.PurseStorage;
import io.github.tracerxbrhd.thecoinage.registry.CoinageItems;
import io.github.tracerxbrhd.thecoinage.registry.CoinageMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class PurseMenu extends AbstractContainerMenu {
    private final Inventory playerInventory;
    private final int purseSlot;
    private final ContainerData balances;

    public PurseMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, buffer.readVarInt(), new SimpleContainerData(6));
    }

    public PurseMenu(int containerId, Inventory inventory, int purseSlot) {
        this(containerId, inventory, purseSlot, new PurseContainerData(inventory, purseSlot));
    }

    private PurseMenu(int containerId, Inventory inventory, int purseSlot, ContainerData balances) {
        super(CoinageMenus.PURSE.get(), containerId);
        this.playerInventory = inventory;
        this.purseSlot = purseSlot;
        this.balances = balances;
        checkContainerDataCount(balances, 6);
        addDataSlots(balances);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    public long balance(Denomination denomination) {
        int offset = denomination.ordinal() * 2;
        return ((long) balances.get(offset + 1) << 32) | (balances.get(offset) & 0xFFFF_FFFFL);
    }

    @Override
    public boolean stillValid(Player player) {
        return purseSlot >= 0 && purseSlot < playerInventory.getContainerSize()
            && playerInventory.getItem(purseSlot).is(CoinageItems.COIN_PURSE.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id >= Denomination.values().length || !stillValid(player)) return false;
        if (player.level().isClientSide()) return true;
        Denomination denomination = Denomination.values()[id];
        ItemStack purse = playerInventory.getItem(purseSlot);
        int maximum = CoinageItems.coin(denomination).getDefaultMaxStackSize();
        PurseStorage.ExtractionResult result = PurseStorage.extract(purse, denomination, maximum,
            CoinageServerConfig.capacity(denomination));
        if (result.extracted() <= 0) return false;
        ItemStack coins = new ItemStack(CoinageItems.coin(denomination), (int) result.extracted());
        player.getInventory().add(coins);
        if (!coins.isEmpty() && player instanceof ServerPlayer serverPlayer) serverPlayer.drop(coins, false);
        playerInventory.setChanged();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int menuSlot) {
        if (menuSlot < 0 || menuSlot >= slots.size() || !stillValid(player)) return ItemStack.EMPTY;
        Slot slot = slots.get(menuSlot);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        Denomination denomination = CoinageItems.denomination(source);
        if (denomination == null || slot.getContainerSlot() == purseSlot) return ItemStack.EMPTY;
        long accepted = PurseStorage.deposit(playerInventory.getItem(purseSlot), denomination, source.getCount(),
            CoinageServerConfig.capacity(denomination)).accepted();
        if (accepted <= 0) return ItemStack.EMPTY;
        source.shrink((int) accepted);
        slot.setChanged();
        playerInventory.setChanged();
        return original.copyWithCount((int) accepted);
    }

    private record PurseContainerData(Inventory inventory, int purseSlot) implements ContainerData {
        @Override public int get(int index) {
            PurseContents data = validPurse() ? PurseStorage.read(inventory.getItem(purseSlot)) : PurseContents.EMPTY;
            long value = data.get(Denomination.values()[index / 2]);
            return index % 2 == 0 ? (int) value : (int) (value >>> 32);
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 6; }
        private boolean validPurse() {
            return purseSlot >= 0 && purseSlot < inventory.getContainerSize()
                && inventory.getItem(purseSlot).is(CoinageItems.COIN_PURSE.get());
        }
    }
}
