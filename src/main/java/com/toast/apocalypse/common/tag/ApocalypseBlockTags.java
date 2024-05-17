package com.toast.apocalypse.common.tag;

import com.toast.apocalypse.common.core.Apocalypse;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public class ApocalypseBlockTags {

    public static final TagKey<Block> BREECHER_TARGETS = modTag("breecher_targets");


    private static TagKey<Block> modTag(String name) {
        return create(Apocalypse.resourceLoc(name));
    }

    private static TagKey<Block> forgeTag(String name) {
        return create(new ResourceLocation("forge", name));
    }

    private static TagKey<Block> create(ResourceLocation id) {
        return BlockTags.create(id);
    }

    public static void init() {}

    // Utility class, instantiation redundant
    private ApocalypseBlockTags() {}
}
