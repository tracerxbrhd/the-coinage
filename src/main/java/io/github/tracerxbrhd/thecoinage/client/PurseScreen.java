package io.github.tracerxbrhd.thecoinage.client;

import io.github.tracerxbrhd.thecoinage.menu.PurseMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public final class PurseScreen extends AbstractContainerScreen<PurseMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND =
        ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final ResourceLocation SLOT_SPRITE =
        ResourceLocation.withDefaultNamespace("container/slot");

    public PurseScreen(PurseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 132;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 36, 0xFFC6C6C6);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, 0xFFFFFFFF);
        graphics.fill(leftPos, topPos, leftPos + 1, topPos + 36, 0xFFFFFFFF);
        graphics.fill(leftPos + imageWidth - 1, topPos, leftPos + imageWidth, topPos + 36, 0xFF555555);
        graphics.fill(leftPos, topPos + 35, leftPos + imageWidth, topPos + 36, 0xFF555555);
        graphics.blit(CONTAINER_BACKGROUND, leftPos, topPos + 36, 0, 126, imageWidth, 96);

        for (int index = 0; index < PurseMenu.PURSE_SLOT_COUNT; index++) {
            Slot slot = menu.getSlot(index);
            graphics.blitSprite(SLOT_SPRITE, leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Intentionally empty: display stacks use Minecraft's native count overlay.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
