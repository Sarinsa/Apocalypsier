package com.toast.apocalypse.common.compat.jei;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.core.register.ApocalypseBlocks;
import com.toast.apocalypse.common.core.register.ApocalypseRecipeTypes;
import com.toast.apocalypse.common.recipe.TrapRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

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
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {

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
}
