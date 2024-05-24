package com.toast.apocalypse.common.compat.jei;

import com.toast.apocalypse.client.screen.DynamicTrapMenuScreen;
import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.core.register.ApocalypseBlocks;
import com.toast.apocalypse.common.core.register.ApocalypseMenus;
import com.toast.apocalypse.common.core.register.ApocalypseRecipeTypes;
import com.toast.apocalypse.common.menus.DynamicTrapMenu;
import com.toast.apocalypse.common.recipe.TrapRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

// Note: dang the JEI api is clean
@JeiPlugin
public class ApocalypseJei implements IModPlugin {


    private static final ResourceLocation ID = Apocalypse.resourceLoc("apocalypse_jei");

    public static final RecipeType<TrapRecipe> TRAP_ASSEMBLING =
            RecipeType.create(Apocalypse.MODID, "trap_assembling", TrapRecipe.class);


    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }


    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();

        registration.addRecipeCategories(new TrapCategory(jeiHelpers));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        registration.addRecipes(TRAP_ASSEMBLING,
                List.copyOf(recipeManager.byType(ApocalypseRecipeTypes.TRAP_ASSEMBLING.get()).values()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ApocalypseBlocks.DYNAMIC_TRAP.get()), TRAP_ASSEMBLING);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(DynamicTrapMenuScreen.class, 118, 35, 16, 16, TRAP_ASSEMBLING);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(DynamicTrapMenu.class, ApocalypseMenus.DYNAMIC_TRAP.get(), TRAP_ASSEMBLING, 0, 9, 9, 36);
    }
}
