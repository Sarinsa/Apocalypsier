package com.toast.apocalypse.common.trap_actions;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.core.register.ApocalypseItems;
import com.toast.apocalypse.common.core.register.ApocalypseTrapActions;
import com.toast.apocalypse.common.entity.living.Ghost;
import com.toast.apocalypse.common.recipe.TrapRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.List;

public class GhostTrap extends BaseTrapAction {

    private static final MutableComponent DESCRIPTION = Component.translatable("apocalypse.trap_type.apocalypse.ghost_freeze.description");
    private static final ResourceLocation ICON = Apocalypse.resourceLoc("textures/trap_icons/ghost_freeze.png");


    public GhostTrap() {
    }

    @Override
    public void execute(Level level, BlockPos pos, boolean facingUp) {
        List<Ghost> nearbyGhosts = level.getEntitiesOfClass(Ghost.class, new AABB(pos).inflate(15));

        if (!nearbyGhosts.isEmpty()) {
            for (Ghost ghost : nearbyGhosts) {
                if (!ghost.isFrozen())
                    ghost.freeze(200);
            }
        }
    }

    @Override
    @Nonnull
    public ResourceLocation iconLocation() {
        return ICON;
    }

    @Override
    public MutableComponent getDescription() {
        return DESCRIPTION;
    }
}
