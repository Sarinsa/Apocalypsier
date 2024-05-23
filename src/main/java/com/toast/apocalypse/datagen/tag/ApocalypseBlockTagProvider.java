package com.toast.apocalypse.datagen.tag;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.core.register.ApocalypseBlocks;
import com.toast.apocalypse.common.tag.ApocalypseBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ApocalypseBlockTagProvider extends BlockTagsProvider {

    public ApocalypseBlockTagProvider(DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator.getPackOutput(), lookupProvider, Apocalypse.MODID, existingFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addTags(HolderLookup.Provider lookupProvider) {
        tag(BlockTags.BEACON_BASE_BLOCKS)
                .add(
                        ApocalypseBlocks.MIDNIGHT_STEEL_BLOCK.get()
                );

        tag(ApocalypseBlockTags.BREECHER_TARGETS)
                .addTags(
                        BlockTags.BEDS,
                        BlockTags.DOORS,
                        Tags.Blocks.CHESTS,
                        Tags.Blocks.BARRELS,
                        Tags.Blocks.FENCE_GATES
                );

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(
                        ApocalypseBlocks.LUNAR_PHASE_SENSOR.get(),
                        ApocalypseBlocks.MIDNIGHT_STEEL_BLOCK.get(),
                        ApocalypseBlocks.DYNAMIC_TRAP.get()
                );
    }
}
