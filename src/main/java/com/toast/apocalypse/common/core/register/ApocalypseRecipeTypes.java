package com.toast.apocalypse.common.core.register;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.recipe.TrapRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ApocalypseRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Apocalypse.MODID);


    public static final RegistryObject<RecipeType<TrapRecipe>> TRAP_ASSEMBLING = register("trap_assembling");


    private static <T extends Recipe<?>> RegistryObject<RecipeType<T>> register(String name) {
        return RECIPE_TYPES.register(name, () -> new RecipeType<T>() {
            @Override
            public String toString() {
                return name;
            }
        });
    }
}
