package com.toast.apocalypse.datagen.recipe;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.core.register.ApocalypseBlocks;
import com.toast.apocalypse.common.core.register.ApocalypseItems;
import com.toast.apocalypse.common.core.register.ApocalypseTrapActions;
import net.minecraft.data.*;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class ApocalypseRecipeProvider extends RecipeProvider {

    public ApocalypseRecipeProvider(DataGenerator dataGenerator) {
        super(dataGenerator.getPackOutput());
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {

        //----------------------- SHAPELESS ----------------------
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ApocalypseItems.MIDNIGHT_STEEL_INGOT.get(), 1)
                .requires(ApocalypseItems.FRAGMENTED_SOUL.get())
                .requires(Tags.Items.INGOTS_IRON)
                .unlockedBy("has_" + itemName(ApocalypseItems.FRAGMENTED_SOUL.get()), has(ApocalypseItems.FRAGMENTED_SOUL.get()))
                .unlockedBy("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
                .save(consumer);

        simpleShapelessRecipe(RecipeCategory.MISC, ApocalypseItems.MIDNIGHT_STEEL_INGOT.get(), 9, consumer, "midnight_steel_ingots_from_midnight_steel_block", ApocalypseBlocks.MIDNIGHT_STEEL_BLOCK.get());
        simpleShapelessRecipe(RecipeCategory.COMBAT, ApocalypseItems.BUCKET_HELM.get(), 1, consumer, Items.BUCKET);


        //------------------------ SHAPED ------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ApocalypseBlocks.LUNAR_PHASE_SENSOR.get().asItem(), 1)
                .pattern("###")
                .pattern("LLL")
                .pattern("SSS")
                .define('#', Tags.Items.GLASS)
                .define('L', ApocalypseItems.MIDNIGHT_STEEL_INGOT.get())
                .define('S', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_" + itemName(ApocalypseItems.MIDNIGHT_STEEL_INGOT.get()), has(ApocalypseItems.MIDNIGHT_STEEL_INGOT.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ApocalypseItems.LUNAR_CLOCK.get(), 1)
                .pattern(" # ")
                .pattern("#R#")
                .pattern(" # ")
                .define('#', ApocalypseItems.MIDNIGHT_STEEL_INGOT.get())
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_" + itemName(ApocalypseItems.MIDNIGHT_STEEL_INGOT.get()), has(ApocalypseItems.MIDNIGHT_STEEL_INGOT.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ApocalypseBlocks.MIDNIGHT_STEEL_BLOCK.get(), 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ApocalypseItems.MIDNIGHT_STEEL_INGOT.get())
                .unlockedBy("has_" + itemName(ApocalypseItems.MIDNIGHT_STEEL_INGOT.get()), has(ApocalypseItems.MIDNIGHT_STEEL_INGOT.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ApocalypseBlocks.DYNAMIC_TRAP.get(),1)
                .pattern("LDL")
                .pattern("SCS")
                .pattern("SRS")
                .define('L', ItemTags.LOGS_THAT_BURN)
                .define('D', Items.DISPENSER)
                .define('S', Tags.Items.COBBLESTONE)
                .define('C', Items.CRAFTING_TABLE)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_" + itemName(Items.DISPENSER), has(Items.DISPENSER))
                .save(consumer);


        //------------------------ TRAP ASSEMBLING ------------------------

        TrapAssemblingRecipeBuilder.trap(ApocalypseTrapActions.GHOST_FREEZE.get(), 600)
                .requires(ApocalypseItems.FRAGMENTED_SOUL.get(), 3)
                .requires(Tags.Items.STRING, 2)
                .requires(Items.SNOW_BLOCK)
                .requires(Blocks.ICE)
                .save(consumer);

        TrapAssemblingRecipeBuilder.trap(ApocalypseTrapActions.EQUIPMENT_BREAK.get(), 350)
                .requires(Tags.Items.GEMS_QUARTZ)
                .requires(Tags.Items.GEMS_AMETHYST)
                .requires(Items.IRON_BLOCK)
                .requires(ItemTags.AXES)
                .requires(ItemTags.PICKAXES)
                .save(consumer);
    }

    private void simpleShapelessRecipe(RecipeCategory category, ItemLike result, int count, Consumer<FinishedRecipe> consumer, ItemLike... ingredients) {
        final ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(category, result, count);

        for (ItemLike ingredient : ingredients) {
            builder.requires(ingredient);
            String ingredientName = Objects.requireNonNull(itemName(ingredient));
            builder.unlockedBy("has_" + ingredientName, has(ingredient));
        }
        builder.save(consumer);
    }

    private void simpleShapelessRecipe(RecipeCategory category, ItemLike result, int count, Consumer<FinishedRecipe> consumer, String recipeName, ItemLike... ingredients) {
        final ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(category, result, count);

        for (ItemLike ingredient : ingredients) {
            builder.requires(ingredient);
            String ingredientName = Objects.requireNonNull(itemName(ingredient));
            builder.unlockedBy("has_" + ingredientName, has(ingredient));
        }
        builder.save(consumer, Apocalypse.resourceLoc(recipeName));
    }

    @Nullable
    @SuppressWarnings("ConstantConditions")
    private static String itemName(ItemLike itemLike) {
        Item item = itemLike.asItem();
        return ForgeRegistries.ITEMS.containsValue(item) ? ForgeRegistries.ITEMS.getKey(item).getPath() : null;
    }
}
