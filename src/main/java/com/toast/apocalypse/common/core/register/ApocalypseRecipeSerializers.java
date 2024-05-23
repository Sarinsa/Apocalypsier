package com.toast.apocalypse.common.core.register;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.recipe.TrapRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ApocalypseRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Apocalypse.MODID);


    public static final RegistryObject<RecipeSerializer<TrapRecipe>> TRAP_ASSEMBLING= register("trap_assembling", TrapRecipe.Serializer::new);


    public static <T extends Recipe<?>> RegistryObject<RecipeSerializer<T>> register(String name, Supplier<RecipeSerializer<T>> supplier) {
        return RECIPE_SERIALIZERS.register(name, supplier);
    }
}
