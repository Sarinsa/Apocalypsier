package com.toast.apocalypse.common.trap_actions;

import com.toast.apocalypse.common.recipe.TrapRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

public abstract class BaseTrapAction {

    private TrapRecipe recipe;

    public BaseTrapAction(TrapRecipe recipe) {
        Objects.requireNonNull(recipe);
        this.recipe = recipe;
    }

    public abstract void execute(Level level, BlockPos pos, boolean facingUp);

    @Nonnull
    public abstract ResourceLocation iconLocation();

    @Nonnull
    public final TrapRecipe getTrapRecipe() {
        return recipe;
    }
}
