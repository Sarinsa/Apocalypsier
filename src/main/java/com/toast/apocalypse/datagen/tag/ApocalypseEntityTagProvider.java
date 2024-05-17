package com.toast.apocalypse.datagen.tag;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.core.register.ApocalypseEntities;
import com.toast.apocalypse.common.tag.ApocalypseEntityTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ApocalypseEntityTagProvider extends EntityTypeTagsProvider {

    public ApocalypseEntityTagProvider(DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper fileHelper) {
        super(dataGenerator.getPackOutput(), lookupProvider, Apocalypse.MODID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(ApocalypseEntityTags.FLYING_ENTITIES).add(
                ApocalypseEntities.DESTROYER.get(),
                ApocalypseEntities.SEEKER.get(),
                ApocalypseEntities.GHOST.get(),
                ApocalypseEntities.GRUMP.get()
        );
    }
}
