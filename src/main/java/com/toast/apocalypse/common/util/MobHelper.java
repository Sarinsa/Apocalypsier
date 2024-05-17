package com.toast.apocalypse.common.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * Contains various helper methods common to many mobs.
 */
public class MobHelper {

    /**
     * Checks the area around a mob to see if it can spawn (no preventing collision).
     *
     * @param entity The entity to check around.
     * @return True if the area is valid for spawning.
     */
    public static boolean canSpawn(LivingEntity entity) {
        Level level = entity.level();
        return level.noCollision(entity.getBoundingBox()) && level.noCollision(entity, entity.getBoundingBox());
    }

    /**
     * This method comes with a max cap. If the amount of entities that have been found exceeds the cap,
     * entities in the list will be removed from the top of the list until the list size matches the cap.
     *
     */
    public static <T extends Entity> List<? extends T> getLoadedEntitiesCapped(Class<? extends T> entityClass, LevelAccessor level, AABB box, @Nullable Predicate<? super T> predicate, final int cap) {
        List<? extends T> list = level.getEntitiesOfClass(entityClass, box, predicate);

        if (list.isEmpty()) {
            return list;
        }
        int count = list.size();

        // Limit the amount of mobs to alert
        while (count > cap) {
            --count;
            list.remove(count);
        }
        return list;
    }
}