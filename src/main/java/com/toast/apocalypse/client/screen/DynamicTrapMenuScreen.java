package com.toast.apocalypse.client.screen;

import com.toast.apocalypse.common.blockentity.DynamicTrapBlockEntity;
import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.menus.DynamicTrapMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DynamicTrapMenuScreen extends AbstractContainerScreen<DynamicTrapMenu> implements MenuAccess<DynamicTrapMenu> {

    private static final ResourceLocation texture = Apocalypse.resourceLoc("textures/gui/container/dynamic_trap.png");


    public DynamicTrapMenuScreen(DynamicTrapMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = leftPos;
        int j = topPos;

        guiGraphics.blit(texture, i, j, 0, 0, imageWidth, imageHeight);

        if (menu.getPreparationTime() > 0 && menu.getMaxPreparationTime() > 0) {
            double progress = (double) menu.getPreparationTime() / menu.getMaxPreparationTime();
            int arrowWidth = (int) (progress * 18);
            guiGraphics.blit(texture, leftPos + 118, topPos + 35, 0, 166, arrowWidth, 16);
        }

        if (menu.getTrapPos() != null) {
            if (Minecraft.getInstance().level != null
                    && Minecraft.getInstance().level.getExistingBlockEntity(menu.getTrapPos()) instanceof DynamicTrapBlockEntity trap) {

                if (trap.getCurrentTrap() != null) {
                    ResourceLocation trapIcon = trap.getCurrentTrap().iconLocation();

                    guiGraphics.blit(trapIcon, leftPos + 142, topPos + 35, 0, 0, 15, 15, 16, 16);
                }
            }
        }
    }
}
