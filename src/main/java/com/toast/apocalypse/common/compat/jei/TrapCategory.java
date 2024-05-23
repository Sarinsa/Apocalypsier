package com.toast.apocalypse.common.compat.jei;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.core.register.ApocalypseBlocks;
import com.toast.apocalypse.common.recipe.TrapRecipe;
import com.toast.apocalypse.common.trap_actions.BaseTrapAction;
import com.toast.apocalypse.common.util.References;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TrapCategory implements IRecipeCategory<TrapRecipe> {


    private static final Component TITLE = Component.translatable(References.TRAP_CATEGORY_TITLE);
    private static final ResourceLocation GUI_TEXTURE = Apocalypse.resourceLoc("textures/gui/container/dynamic_trap.png");
    private static final ResourceLocation RESULT_SLOT = Apocalypse.resourceLoc("textures/gui/container/components/trap_result_slot.png");

    private final IJeiHelpers jeiHelpers;
    private final IGuiHelper guiHelper;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotBackground;
    private final IDrawable resultSlotBackground;
    private final IDrawable progressBackground;
    private final IDrawableAnimated progress;

    private final int width = 140, height = 54;


    public TrapCategory(IJeiHelpers jeiHelpers) {
        this.jeiHelpers = jeiHelpers;
        this.guiHelper = jeiHelpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(width, height);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ApocalypseBlocks.DYNAMIC_TRAP.get()));
        this.slotBackground = guiHelper.getSlotDrawable();
        this.resultSlotBackground = guiHelper.drawableBuilder(RESULT_SLOT, 0, 0, 32, 32).setTextureSize(32, 32).build();
        this.progressBackground = guiHelper.createDrawable(GUI_TEXTURE, 0, 183, 17, 16);
        this.progress = guiHelper.createAnimatedDrawable(
                guiHelper.createDrawable(GUI_TEXTURE, 0, 166, 18, 16),
                100,
                IDrawableAnimated.StartDirection.LEFT,
                false
        );
    }


    @Override
    public RecipeType<TrapRecipe> getRecipeType() {
        return ApocalypseJei.TRAP_ASSEMBLING;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TrapRecipe recipe, IFocusGroup focuses) {
        builder.setShapeless();
        int slotIndex = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                builder.addSlot(RecipeIngredientRole.INPUT, (i * 18) + 1, (j * 18) + 1)
                        .addIngredients(recipe.getIngredients().get(slotIndex))
                        .setBackground(slotBackground, -1, -1);

                slotIndex++;
            }
        }
    }

    @Override
    public void draw(TrapRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        progressBackground.draw(guiGraphics, 64, 19);
        progress.draw(guiGraphics, 64, 18);
        resultSlotBackground.draw(guiGraphics, width - (width / 3) - 8, (height / 2) - 16);
        guiGraphics.blit(recipe.getResultTrap().iconLocation(), width - (width / 3), (height / 2) - 8, 0, 0, 15, 15, 16, 16);
    }

    @Override
    public List<Component> getTooltipStrings(TrapRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (hoveringOverSlotAt(width - (width / 3), (height / 2) - 8, mouseX, mouseY)) {
            BaseTrapAction trapAction = recipe.getResultTrap();

            return List.of(
                    Component.translatable(trapAction.getNameTranslationKey(trapAction)),
                    Component.literal(""),
                    trapAction.getDescription().withStyle(ChatFormatting.GRAY));
        }
        return List.of();
    }

    private boolean hoveringOverSlotAt(int posX, int posY, double mouseX, double mouseY) {
        return mouseX >= posX && mouseX <= (posX + 16)
                && mouseY >= posY && mouseY <= posY + 16;
    }
}
