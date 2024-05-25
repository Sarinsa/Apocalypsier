package com.toast.apocalypse.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.toast.apocalypse.api.register.ModRegistries;
import com.toast.apocalypse.common.blockentity.DynamicTrapBlockEntity;
import com.toast.apocalypse.common.core.register.ApocalypseRecipeSerializers;
import com.toast.apocalypse.common.core.register.ApocalypseRecipeTypes;
import com.toast.apocalypse.common.core.register.ApocalypseTrapActions;
import com.toast.apocalypse.api.BaseTrapAction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;

@SuppressWarnings("ClassCanBeRecord")
public final class TrapRecipe implements Recipe<DynamicTrapBlockEntity> {

    private static final int MAX_PREP_TIME = 100000;
    private static final int MAX_INGREDIENTS = 9;

    private final ResourceLocation id;
    private final BaseTrapAction resultTrap;
    private final int preparationTime;
    private final NonNullList<Ingredient> ingredients;

    public TrapRecipe(ResourceLocation id, BaseTrapAction resultTrap, int preparationTime, NonNullList<Ingredient> ingredients) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(resultTrap);

        if (preparationTime < 0 || preparationTime > MAX_PREP_TIME)
            throw new IllegalArgumentException("Tried to create trap recipe with invalid preparation time. Value must not be less than 0 or greater than 10000");

        if (ingredients.size() < 1 || ingredients.size() > MAX_INGREDIENTS)
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

        this.id = id;
        this.resultTrap = resultTrap;
        this.preparationTime = preparationTime;
        this.ingredients = ingredients;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    @Override
    public boolean matches(DynamicTrapBlockEntity container, Level level) {
        List<ItemStack> copy = new ArrayList<>(container.getContents());

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
        return foundIngredients == getIngredients().size();
    }

    @Override
    public ItemStack assemble(DynamicTrapBlockEntity container, RegistryAccess registryAccess) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return null;
    }

    @Nonnull
    public BaseTrapAction getResultTrap() {
        return resultTrap;
    }

    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ApocalypseRecipeSerializers.TRAP_ASSEMBLING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ApocalypseRecipeTypes.TRAP_ASSEMBLING.get();
    }

    @SuppressWarnings("ConstantConditions")
    public static class Serializer implements RecipeSerializer<TrapRecipe> {

        @Override
        public TrapRecipe fromJson(ResourceLocation id, JsonObject jsonObject) {
            if (!jsonObject.has("result") || !jsonObject.get("result").isJsonObject()) throw new JsonSyntaxException("Missing result, expected to find a string or object");

            JsonObject resultObj = GsonHelper.getAsJsonObject(jsonObject, "result");

            NonNullList<Ingredient> ingredients = itemsFromJson(GsonHelper.getAsJsonArray(jsonObject, "ingredients"));
            int preparationTime = GsonHelper.getAsInt(resultObj, "preparation_time", 200);
            ResourceLocation trapId = ResourceLocation.tryParse(GsonHelper.getAsString(resultObj, "trap_type"));

            if (ingredients.isEmpty()) {
                throw new JsonParseException("No ingredients for trap assembling recipe");
            }
            if (ingredients.size() > MAX_INGREDIENTS) {
                throw new JsonParseException("Too many ingredients for trap assembling recipe. The maximum is 9");
            }
            if (trapId == null || !ModRegistries.TRAP_ACTIONS_REGISTRY.get().containsKey(trapId)) {
                throw new JsonParseException("No valid result trap type found for assembling recipe. ID is either malformed or doesn't exist in the registry.");
            }
            else {
                return new TrapRecipe(id, ModRegistries.TRAP_ACTIONS_REGISTRY.get().getValue(trapId), preparationTime, ingredients);
            }
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray jsonArray) {
            NonNullList<Ingredient> list = NonNullList.create();

            for(int i = 0; i < jsonArray.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(jsonArray.get(i), false);
                list.add(ingredient);
            }
            for (int i = list.size(); i < TrapRecipe.MAX_INGREDIENTS; i++) {
                list.add(Ingredient.EMPTY);
            }
            return list;
        }

        @Override
        public @Nullable TrapRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf byteBuf) {
            ResourceLocation trapId = byteBuf.readResourceLocation();
            int preparationTime = byteBuf.readInt();
            int ingredientsCount = byteBuf.readInt();
            NonNullList<Ingredient> ingredients = NonNullList.create();

            for(int j = 0; j < ingredientsCount; ++j) {
                ingredients.set(j, Ingredient.fromNetwork(byteBuf));
            }
            for (int i = ingredients.size(); i < TrapRecipe.MAX_INGREDIENTS; i++) {
                ingredients.add(Ingredient.EMPTY);
            }
            return new TrapRecipe(id, ModRegistries.TRAP_ACTIONS_REGISTRY.get().getValue(trapId), preparationTime, ingredients);
        }

        @Override
        public void toNetwork(FriendlyByteBuf byteBuf, TrapRecipe trapRecipe) {
            byteBuf.writeResourceLocation(ModRegistries.TRAP_ACTIONS_REGISTRY.get().getKey(trapRecipe.getResultTrap()));
            byteBuf.writeInt(trapRecipe.preparationTime);
            byteBuf.writeInt(trapRecipe.ingredients.size());

            for(Ingredient ingredient : trapRecipe.ingredients) {
                ingredient.toNetwork(byteBuf);
            }
        }
    }
}