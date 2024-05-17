package com.toast.apocalypse.datagen.loot;

import com.toast.apocalypse.common.core.register.ApocalypseItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApocalypseLootTableProvider extends LootTableProvider {

    public ApocalypseLootTableProvider(DataGenerator dataGenerator) {
        super(dataGenerator.getPackOutput(), null, List.of(
                new SubProviderEntry(() -> new ApocalypseBlockLootTableProvider(Set.of(ApocalypseItems.MIDNIGHT_STEEL_INGOT.get()), FeatureFlags.VANILLA_SET), LootContextParamSets.BLOCK),
                new SubProviderEntry(() -> new ApocalypseEntityLootTableProvider(FeatureFlags.VANILLA_SET), LootContextParamSets.ENTITY)
        ));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext context) {
        // Not validating
    }
}
