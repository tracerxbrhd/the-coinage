package io.github.tracerxbrhd.thecoinage.client;

import io.github.tracerxbrhd.thecoinage.menu.PurseMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public final class PurseScreen extends AbstractContainerScreen<PurseMenu> {
    private static final Identifier CONTAINER_BACKGROUND =
        Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final Identifier SLOT_SPRITE =
        Identifier.withDefaultNamespace("container/slot");

    public PurseScreen(PurseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 132);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 36, 0xFFC6C6C6);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, 0xFFFFFFFF);
        graphics.fill(leftPos, topPos, leftPos + 1, topPos + 36, 0xFFFFFFFF);
        graphics.fill(leftPos + imageWidth - 1, topPos, leftPos + imageWidth, topPos + 36, 0xFF555555);
        graphics.fill(leftPos, topPos + 35, leftPos + imageWidth, topPos + 36, 0xFF555555);
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, leftPos, topPos + 36,
            0.0F, 126.0F, imageWidth, 96, 256, 256);

        for (int index = 0; index < PurseMenu.PURSE_SLOT_COUNT; index++) {
            Slot slot = menu.getSlot(index);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE,
                leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // Intentionally empty: display stacks use Minecraft's native count overlay.
    }
}
