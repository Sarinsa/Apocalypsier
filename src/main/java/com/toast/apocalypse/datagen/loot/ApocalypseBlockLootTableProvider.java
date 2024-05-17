package com.toast.apocalypse.datagen.loot;

import com.toast.apocalypse.common.core.register.ApocalypseBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.HashSet;
import java.util.Set;

public class ApocalypseBlockLootTableProvider extends BlockLootSubProvider {

    private final Set<Block> knownBlocks = new HashSet<>();

    protected ApocalypseBlockLootTableProvider(Set<Item> set, FeatureFlagSet flagSet) {
        super(set, flagSet);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return this.knownBlocks;
    }

    @Override
    protected void add(Block block, LootTable.Builder table) {
        super.add(block, table);
        this.knownBlocks.add(block);
    }

    @Override
    protected void generate() {
        this.dropSelf(ApocalypseBlocks.LUNAR_PHASE_SENSOR.get());
        this.dropSelf(ApocalypseBlocks.MIDNIGHT_STEEL_BLOCK.get());
    }
}
