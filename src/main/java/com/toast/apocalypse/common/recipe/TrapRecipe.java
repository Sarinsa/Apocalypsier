package com.toast.apocalypse.common.recipe;

import com.toast.apocalypse.common.util.DataStructureUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public final class TrapRecipe {

    private static final int MAX_PREP_TIME = 100000;
    private static final int MAX_INGREDIENTS = 9;

    private final int preparationTime;
    private final Ingredient[] ingredients;

    public TrapRecipe(int preparationTime, Ingredient[] ingredients) {
        if (preparationTime < 0 || preparationTime > MAX_PREP_TIME)
            throw new IllegalArgumentException("Tried to create trap recipe with invalid preparation time. Value must not be less than 0 or greater than 10000");

        if (ingredients.length < 1 || ingredients.length > MAX_INGREDIENTS)
            throw new IllegalArgumentException("Tried to create trap recipe with more than 9 item types. This is not allowed!");

        boolean emptyRecipe = true;

        for (Ingredient ingredient : ingredients) {
            if (!ingredient.isEmpty()) {
                emptyRecipe = false;
                break;
            }
        }
        if (emptyRecipe)
            throw new IllegalArgumentException("Tried to create trap recipe with empty ingredients. This is not allowed.");

        this.preparationTime = preparationTime;
        this.ingredients = DataStructureUtils.copyOfFill(ingredients, 9, Ingredient.EMPTY);
    }

    /**
     *
     * @param contents A list of item stacks to check. This is and usually should be the "inventory" of a Dynamic Trap.
     * @return True if contents match the ingredients of the recipe.
     */
    public boolean matches(List<ItemStack> contents) {
        if (contents.size() > MAX_INGREDIENTS)
            return false;

        List<ItemStack> copy = new ArrayList<>(contents);

        int foundIngredients = 0;

        for (Ingredient ingredient : getIngredients()) {
            for (ItemStack itemStack : copy) {
                if (ingredient.test(itemStack)) {
                    ++foundIngredients;
                    copy.remove(itemStack);
                    break;
                }
            }
        }
        return foundIngredients == getIngredients().length;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public Ingredient[] getIngredients() {
        return ingredients;
    }
}