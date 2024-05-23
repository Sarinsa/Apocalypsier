package com.toast.apocalypse.common.compat.jei;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public abstract class BaseRecipeCategory<T> implements IRecipeCategory<T> {

    protected final IJeiHelpers jeiHelpers;
    protected final IGuiHelper guiHelper;


    public BaseRecipeCategory(IJeiHelpers jeiHelpers) {
        this.jeiHelpers = jeiHelpers;
        this.guiHelper = jeiHelpers.getGuiHelper();
    }


    /**
     * Helper method for drawing a String that shows in seconds how long
     * this recipe takes to complete, like with furnace smelting or brewing.
     */
    protected void drawPreparationTime(T recipe, int preparationTime, GuiGraphics guiGraphics, int y) {
        if (preparationTime > 0) {
            int cookTimeSeconds = preparationTime / 20;
            Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", cookTimeSeconds);
            Minecraft minecraft = Minecraft.getInstance();
            Font fontRenderer = minecraft.font;
            int stringWidth = fontRenderer.width(timeString);
            guiGraphics.drawString(fontRenderer, timeString, getWidth() - stringWidth, y, 0xFF808080, false);
        }
    }

    protected boolean hoveringOverSlotAt(int posX, int posY, double mouseX, double mouseY) {
        return mouseX >= posX && mouseX <= (posX + 16)
                && mouseY >= posY && mouseY <= posY + 16;
    }
}
