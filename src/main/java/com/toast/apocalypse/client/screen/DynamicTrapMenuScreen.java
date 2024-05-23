package com.toast.apocalypse.client.screen;

import com.toast.apocalypse.common.blockentity.DynamicTrapBlockEntity;
import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.menus.DynamicTrapMenu;
import com.toast.apocalypse.common.trap_actions.BaseTrapAction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DynamicTrapMenuScreen extends AbstractContainerScreen<DynamicTrapMenu> implements MenuAccess<DynamicTrapMenu> {

    private static final ResourceLocation texture = Apocalypse.resourceLoc("textures/gui/container/dynamic_trap.png");


    public DynamicTrapMenuScreen(DynamicTrapMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        if (menu.getTrapPos() != null) {
            if (Minecraft.getInstance().level != null
                    && Minecraft.getInstance().level.getExistingBlockEntity(menu.getTrapPos()) instanceof DynamicTrapBlockEntity trap) {

                if (trap.getCurrentTrap() != null) {
                    if (hoveringOverSlotAt(leftPos + 141, topPos + 35, mouseX, mouseY)) {
                        BaseTrapAction trapType = trap.getCurrentTrap();
                        List<Component> components = new ArrayList<>();

                        components.add(Component.translatable(trapType.getNameTranslationKey(trapType)));

                        if (trapType.getDescription() != null) {
                            components.add(Component.literal(""));
                            components.add(trapType.getDescription().withStyle(ChatFormatting.GRAY));
                        }
                        guiGraphics.renderComponentTooltip(font, components, mouseX, mouseY);
                    }
                }
            }
        }
    }

    private boolean hoveringOverSlotAt(int posX, int posY, double mouseX, double mouseY) {
        return mouseX >= posX && mouseX <= (posX + 16)
                && mouseY >= posY && mouseY <= posY + 16;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        guiGraphics.blit(texture, leftPos + 118, topPos + 35, 0, 183, 17, 16);

        if (menu.getPreparationTime() > 0 && menu.getMaxPreparationTime() > 0) {
            double progress = (double) menu.getPreparationTime() / menu.getMaxPreparationTime();
            int arrowWidth = (int) (progress * 18);
            guiGraphics.blit(texture, leftPos + 118, topPos + 34, 0, 166, arrowWidth, 17);
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
