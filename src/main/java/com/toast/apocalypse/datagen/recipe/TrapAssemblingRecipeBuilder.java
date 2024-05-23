package com.toast.apocalypse.datagen.recipe;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.toast.apocalypse.common.core.register.ApocalypseRecipeSerializers;
import com.toast.apocalypse.common.core.register.ApocalypseTrapActions;
import com.toast.apocalypse.common.trap_actions.BaseTrapAction;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class TrapAssemblingRecipeBuilder implements RecipeBuilder {

    private final BaseTrapAction result;
    private final int preparationTime;
    private final List<Ingredient> ingredients = Lists.newArrayList();

    public TrapAssemblingRecipeBuilder(BaseTrapAction trap, int preparationTime) {
        this.result = trap;
        this.preparationTime = preparationTime;
    }

    public static TrapAssemblingRecipeBuilder trap(BaseTrapAction trap, int preparationTime) {
        return new TrapAssemblingRecipeBuilder(trap, preparationTime);
    }

    public TrapAssemblingRecipeBuilder requires(TagKey<Item> tagKey) {
        return requires(Ingredient.of(tagKey));
    }

    public TrapAssemblingRecipeBuilder requires(TagKey<Item> tagKey, int count) {
        for (int i = 0; i < count; ++i) {
            requires(Ingredient.of(tagKey));
        }
        return this;
    }

    public TrapAssemblingRecipeBuilder requires(ItemLike itemLike) {
        return requires(itemLike, 1);
    }

    public TrapAssemblingRecipeBuilder requires(ItemLike itemLike, int count) {
        for (int i = 0; i < count; ++i) {
            requires(Ingredient.of(itemLike));
        }
        return this;
    }

    public TrapAssemblingRecipeBuilder requires(Ingredient ingredient) {
        return requires(ingredient, 1);
    }

    public TrapAssemblingRecipeBuilder requires(Ingredient ingredient, int count) {
        for (int i = 0; i < count; ++i) {
            ingredients.add(ingredient);
        }
        return this;
    }

    // Does nothing
    @Override
    public TrapAssemblingRecipeBuilder unlockedBy(String s, CriterionTriggerInstance triggerInstance) {
        return this;
    }

    // Does nothing
    @Override
    public RecipeBuilder group(String s) {
        return null;
    }

    // Does nothing
    @Override
    public Item getResult() {
        return null;
    }

    public BaseTrapAction getTrapResult() {
        return result;
    }

    @Override
    public void save(Consumer<FinishedRecipe> recipeSaver, ResourceLocation id) {
        recipeSaver.accept(new Result(id.withSuffix("_trap"), result, preparationTime, ingredients));
    }

    @Override
    public void save(Consumer<FinishedRecipe> recipeSaver) {
        save(recipeSaver, ApocalypseTrapActions.TRAP_ACTIONS_REGISTRY.get().getKey(getTrapResult()));
    }


    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final BaseTrapAction result;
        private final int preparationTime;
        private final List<Ingredient> ingredients;

        public Result(ResourceLocation id, BaseTrapAction result, int preparationTime, List<Ingredient> ingredients) {
            this.id = id;
            this.result = result;
            this.preparationTime = preparationTime;
            this.ingredients = ingredients;

            if (ingredients.size() < 1 || ingredients.size() > 9)
                throw new IllegalArgumentException("Trap assembling recipe had less than 1 or more than 9 ingredients. This is not allowed.");
        }

        public void serializeRecipeData(JsonObject jsonObject) {
            JsonArray jsonarray = new JsonArray();

            for(Ingredient ingredient : ingredients) {
                jsonarray.add(ingredient.toJson());
            }

            jsonObject.add("ingredients", jsonarray);
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("trap_type", ApocalypseTrapActions.TRAP_ACTIONS_REGISTRY.get().getKey(result).toString());

            if (preparationTime < 0)
                throw new IllegalArgumentException("Preparation time can not be negative");

            jsonobject.addProperty("preparation_time", this.preparationTime);


            jsonObject.add("result", jsonobject);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ApocalypseRecipeSerializers.TRAP_ASSEMBLING.get();
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        @Nullable
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        @Nullable
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}