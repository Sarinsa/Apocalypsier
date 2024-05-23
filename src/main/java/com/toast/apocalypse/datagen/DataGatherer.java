package com.toast.apocalypse.datagen;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.datagen.loot.ApocalypseLootModProvider;
import com.toast.apocalypse.datagen.loot.ApocalypseLootTableProvider;
import com.toast.apocalypse.datagen.recipe.ApocalypseRecipeProvider;
import com.toast.apocalypse.datagen.tag.ApocalypseBlockTagProvider;
import com.toast.apocalypse.datagen.tag.ApocalypseEntityTagProvider;
import com.toast.apocalypse.datagen.tag.ApocalypseItemTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Apocalypse.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGatherer {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator dataGenerator = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        if (event.includeServer()) {
            dataGenerator.addProvider(true, new ApocalypseRecipeProvider(dataGenerator));
            dataGenerator.addProvider(true, new ApocalypseLootTableProvider(dataGenerator));
            dataGenerator.addProvider(true, new ApocalypseAdvancementProvider(dataGenerator, lookupProvider, fileHelper));
            BlockTagsProvider blockTagProvider = new ApocalypseBlockTagProvider(dataGenerator, lookupProvider, fileHelper);
            dataGenerator.addProvider(true, blockTagProvider);
            dataGenerator.addProvider(true, new ApocalypseItemTagProvider(dataGenerator, lookupProvider, blockTagProvider.contentsGetter(), fileHelper));
            dataGenerator.addProvider(true, new ApocalypseEntityTagProvider(dataGenerator, lookupProvider, fileHelper));
            dataGenerator.addProvider(true, new ApocalypseLootModProvider(dataGenerator));
        }
    }
}
