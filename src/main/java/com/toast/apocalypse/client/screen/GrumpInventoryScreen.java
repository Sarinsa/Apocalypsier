package com.toast.apocalypse.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.entity.living.Grump;
import com.toast.apocalypse.common.inventory.container.GrumpInventoryContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Quaternionf;

public class GrumpInventoryScreen extends AbstractContainerScreen<GrumpInventoryContainer> {

    private static final ResourceLocation TEXTURE = Apocalypse.resourceLoc("textures/gui/container/grump.png");
    private final Grump grump;
    private float xMouse;
    private float yMouse;

    public GrumpInventoryScreen(GrumpInventoryContainer container, Inventory playerInventory, Grump grump) {
        super(container, playerInventory, grump.getDisplayName());
        this.grump = grump;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int xMouse, int yMouse) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (width - imageWidth) / 2;
        int j = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, i, j, 0, 0, imageWidth, imageHeight);
        guiGraphics.blit(TEXTURE, i + 7, j + 35 - 18, 18, imageHeight + 54, 18, 18);
        guiGraphics.blit(TEXTURE, i + 7, j + 35, 0, imageHeight + 54, 18, 18);

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, i + 51, j + 50, 17, (float)(i + 51) - xMouse, (float)(j + 75 - 50) - yMouse, grump);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int xMouse, int yMouse, float partialTick) {
        renderBackground(guiGraphics);
        this.xMouse = (float)xMouse;
        this.yMouse = (float)yMouse;
        super.render(guiGraphics, xMouse, yMouse, partialTick);
        renderTooltip(guiGraphics, xMouse, yMouse);
    }
}
