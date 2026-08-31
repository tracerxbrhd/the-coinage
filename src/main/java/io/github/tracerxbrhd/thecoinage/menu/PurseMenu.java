package io.github.tracerxbrhd.thecoinage.menu;

import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.config.CoinageServerConfig;
import io.github.tracerxbrhd.thecoinage.purse.PurseContents;
import io.github.tracerxbrhd.thecoinage.purse.PurseStorage;
import io.github.tracerxbrhd.thecoinage.registry.CoinageItems;
import io.github.tracerxbrhd.thecoinage.registry.CoinageMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class PurseMenu extends AbstractContainerMenu {
    public static final int PURSE_SLOT_COUNT = Denomination.values().length;
    private static final int[] PURSE_SLOT_X = {44, 80, 116};
    private static final int PURSE_SLOT_Y = 18;

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

        SimpleContainer virtualSlots = new SimpleContainer(PURSE_SLOT_COUNT);
        for (Denomination denomination : Denomination.values()) {
            int index = denomination.ordinal();
            addSlot(new Slot(virtualSlots, index, PURSE_SLOT_X[index], PURSE_SLOT_Y) {
                @Override
                public ItemStack getItem() {
                    long stored = balance(denomination);
                    if (stored <= 0) return ItemStack.EMPTY;
                    int displayCount = (int) Math.min(stored, Integer.MAX_VALUE);
                    return new ItemStack(CoinageItems.coin(denomination), displayCount);
                }

                @Override
                public void set(ItemStack stack) {
                    // The real contents live in the purse data component, not in this display slot.
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return CoinageItems.denomination(stack) == denomination;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return balance(denomination) > 0;
                }

                @Override
                public boolean isFake() {
                    return true;
                }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 50 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 108));
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
    public void clicked(int menuSlot, int button, ContainerInput input, Player player) {
        if (menuSlot >= 0 && menuSlot < PURSE_SLOT_COUNT) {
            if (!player.level().isClientSide() && stillValid(player)) {
                Denomination denomination = Denomination.values()[menuSlot];
                if (input == ContainerInput.PICKUP) {
                    handlePurseClick(denomination, button);
                } else if (input == ContainerInput.QUICK_MOVE) {
                    withdrawToInventory(denomination);
                }
                playerInventory.setChanged();
                broadcastChanges();
            }
            return;
        }
        super.clicked(menuSlot, button, input, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int menuSlot) {
        if (menuSlot < 0 || menuSlot >= slots.size() || !stillValid(player) || player.level().isClientSide()) {
            return ItemStack.EMPTY;
        }
        if (menuSlot < PURSE_SLOT_COUNT) return withdrawToInventory(Denomination.values()[menuSlot]);

        Slot slot = slots.get(menuSlot);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        Denomination denomination = CoinageItems.denomination(source);
        if (denomination == null || slot.getContainerSlot() == purseSlot) return ItemStack.EMPTY;

        long accepted = deposit(denomination, source.getCount());
        if (accepted <= 0) return ItemStack.EMPTY;
        source.shrink((int) accepted);
        slot.setChanged();
        playerInventory.setChanged();
        broadcastChanges();
        return new ItemStack(CoinageItems.coin(denomination), (int) accepted);
    }

    private void handlePurseClick(Denomination denomination, int button) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            withdrawToCursor(denomination, button == 1 ? 1 : CoinageItems.coin(denomination).getDefaultMaxStackSize());
            return;
        }
        if (CoinageItems.denomination(carried) != denomination) return;
        int requested = button == 1 ? 1 : carried.getCount();
        long accepted = deposit(denomination, requested);
        if (accepted > 0) carried.shrink((int) accepted);
    }

    private void withdrawToCursor(Denomination denomination, int requested) {
        PurseStorage.ExtractionResult result = PurseStorage.extract(purseStack(), denomination, requested,
            CoinageServerConfig.capacity(denomination));
        if (result.extracted() > 0) {
            setCarried(new ItemStack(CoinageItems.coin(denomination), (int) result.extracted()));
        }
    }

    private ItemStack withdrawToInventory(Denomination denomination) {
        int maximum = CoinageItems.coin(denomination).getDefaultMaxStackSize();
        PurseStorage.ExtractionResult result = PurseStorage.extract(purseStack(), denomination, maximum,
            CoinageServerConfig.capacity(denomination));
        if (result.extracted() <= 0) return ItemStack.EMPTY;

        ItemStack coins = new ItemStack(CoinageItems.coin(denomination), (int) result.extracted());
        int extracted = coins.getCount();
        moveItemStackTo(coins, PURSE_SLOT_COUNT, slots.size(), true);
        int moved = extracted - coins.getCount();
        if (!coins.isEmpty()) deposit(denomination, coins.getCount());
        return moved > 0 ? new ItemStack(CoinageItems.coin(denomination), moved) : ItemStack.EMPTY;
    }

    private long deposit(Denomination denomination, long requested) {
        return PurseStorage.deposit(purseStack(), denomination, requested,
            CoinageServerConfig.capacity(denomination)).accepted();
    }

    private ItemStack purseStack() {
        return playerInventory.getItem(purseSlot);
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
