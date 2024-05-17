package com.toast.apocalypse.common.tag;

import com.toast.apocalypse.common.core.Apocalypse;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.tags.ITag;

public class ApocalypseEntityTags {

    public static final TagKey<EntityType<?>> FLYING_ENTITIES = modTag("flying_entities");

    private static TagKey<EntityType<?>> modTag(String name) {
        return create(Apocalypse.resourceLoc(name));
    }

    private static TagKey<EntityType<?>> forgeTag(String name) {
        return create(new ResourceLocation("forge", name));
    }

    private static TagKey<EntityType<?>> create(ResourceLocation id) {
        return TagKey.create(Registries.ENTITY_TYPE, id);
    }

    public static void init() {}

    // Utility class, instantiation redundant
    private ApocalypseEntityTags() {}
}
