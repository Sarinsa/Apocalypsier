package com.toast.apocalypse.common.trap_actions;

import com.toast.apocalypse.api.BaseTrapAction;
import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.core.config.ApocalypseCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EquipmentBreakTrap extends BaseTrapAction {

    private static final ResourceLocation ICON = Apocalypse.resourceLoc("textures/trap_icons/equipment_break.png");
    private static final String DESCRIPTION_KEY = "apocalypse.trap_type.apocalypse.equipment_break.description";

    public EquipmentBreakTrap() {
    }

    @Override
    public void execute(Level level, BlockPos pos, boolean facingUp) {
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(ApocalypseCommonConfig.COMMON.getArmorShatterTrapRange()));

        if (!entities.isEmpty()) {
            for (LivingEntity livingEntity : entities) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.getType() == EquipmentSlot.Type.ARMOR) {

                        if (livingEntity.getItemBySlot(slot).isDamageableItem()) {
                            livingEntity.getItemBySlot(slot).hurtAndBreak(100000, livingEntity, (entity) -> entity.broadcastBreakEvent(slot));
                        }
                    }
                }
            }
        }
    }

    @NotNull
    @Override
    public ResourceLocation iconLocation() {
        return ICON;
    }

    @Override
    public String getDescriptionKey() {
        return DESCRIPTION_KEY;
    }
}
