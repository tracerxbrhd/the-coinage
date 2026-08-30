package io.github.tracerxbrhd.thecoinage.client;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyMath;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.menu.PurseMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class PurseScreen extends AbstractContainerScreen<PurseMenu> {
    public PurseScreen(PurseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        for (Denomination denomination : Denomination.values()) {
            int row = denomination.ordinal();
            addRenderableWidget(Button.builder(Component.literal("-"), button -> {
                if (menu.clickMenuButton(minecraft.player, row)) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, row);
                }
            }).bounds(leftPos + 146, topPos + 17 + row * 18, 18, 16).build());
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF201A16);
        graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + 70, 0xFF3A2A20);
        graphics.fill(leftPos + 4, topPos + 75, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF8B8B8B);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int x = leftPos + 7 + column * 18;
                int y = topPos + 83 + row * 18;
                graphics.fill(x, y, x + 18, y + 18, 0xFF373737);
            }
        }
        for (int column = 0; column < 9; column++) {
            int x = leftPos + 7 + column * 18;
            graphics.fill(x, topPos + 141, x + 18, topPos + 159, 0xFF373737);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 8, 6, 0xF2C46D, false);
        for (Denomination denomination : Denomination.values()) {
            graphics.text(font, Component.translatable("screen.the_coinage.purse.balance",
                Component.translatable(denomination.translationKey()),
                CurrencyMath.formatNormalized(menu.balance(denomination))), 12, 21 + denomination.ordinal() * 18,
                0xF4E5CB, false);
        }
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFFFFF, false);
    }
}
