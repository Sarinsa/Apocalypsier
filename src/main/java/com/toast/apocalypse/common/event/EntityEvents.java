package com.toast.apocalypse.common.event;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.core.config.ApocalypseCommonConfig;
import com.toast.apocalypse.common.core.difficulty.MobAttributeHandler;
import com.toast.apocalypse.common.core.difficulty.MobEquipmentHandler;
import com.toast.apocalypse.common.core.difficulty.MobPotionHandler;
import com.toast.apocalypse.common.core.difficulty.PlayerDifficultyManager;
import com.toast.apocalypse.common.core.register.ApocalypseEntities;
import com.toast.apocalypse.common.core.register.ApocalypseItems;
import com.toast.apocalypse.common.entity.living.Grump;
import com.toast.apocalypse.common.entity.living.IFullMoonMob;
import com.toast.apocalypse.common.util.CapabilityHelper;
import com.toast.apocalypse.common.util.NBTUtil;
import com.toast.apocalypse.common.util.References;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Graph;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class EntityEvents {

    /** Whether attribute bonuses should only be applied to enemy mobs. */
    public static boolean ENIMIES_ONLY;

    /**
     * A Map containing all the difficulty-limited EntityTypes and their
     * difficulty level needed to start spawning.
     */
    public static Map<EntityType<?>, Double> MOB_DIFFICULTIES = new HashMap<>();


    /**
     * Cancel full moon monsters despawning during full moons.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onDespawnCheck(MobSpawnEvent.AllowDespawn event) {
        if (!event.getLevel().isClientSide()) {
            if (event.getEntity() instanceof IFullMoonMob && Apocalypse.INSTANCE.getDifficultyManager().isFullMoonNight()) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    /**
     * Denies mob spawns of mobs that requires the nearest player
     * to have passed a certain difficulty to spawn.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCheckSpawn(MobSpawnEvent.SpawnPlacementCheck event) {
        MobSpawnType spawnType = event.getSpawnType();

        if (spawnType == MobSpawnType.SPAWNER || spawnType == MobSpawnType.SPAWN_EGG || spawnType == MobSpawnType.COMMAND
                || spawnType == MobSpawnType.MOB_SUMMONED || spawnType == MobSpawnType.STRUCTURE)
            return;

        EntityType<?> entityType = event.getEntityType();

        if (MOB_DIFFICULTIES.containsKey(entityType)) {
            final double neededDifficulty = MOB_DIFFICULTIES.get(entityType);
            final long nearestDifficulty = (PlayerDifficultyManager.getNearestPlayerDifficulty(event.getLevel(), event.getPos())) / References.DAY_LENGTH;

            if (nearestDifficulty < neededDifficulty)
                event.setResult(Event.Result.DENY);
        }
    }

    /**
     * Handles equipment and potion effects for mobs.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide)
            return;

        if (!(event.getEntity() instanceof LivingEntity livingEntity) || event.getEntity() instanceof Player)
            return;

        if (NBTUtil.isEntityProcessed(livingEntity))
            return;

        Level level = livingEntity.getCommandSenderWorld();
        RandomSource random = level.getRandom();
        final long difficulty = PlayerDifficultyManager.getNearestPlayerDifficulty(level, livingEntity);
        final boolean fullMoon = Apocalypse.INSTANCE.getDifficultyManager().isFullMoonNight();

        // Don't do anything if the player is still on grace period
        if (difficulty <= 0L)
            return;

        if (!(livingEntity instanceof Enemy) && ENIMIES_ONLY)
            return;

        MobAttributeHandler.handleAttributes(livingEntity, difficulty, fullMoon);
        MobPotionHandler.handlePotions(livingEntity, difficulty, fullMoon, random);
        MobEquipmentHandler.handleMobEquipment(livingEntity, difficulty, fullMoon, random);

        // Arright, the deed is done! Now lets just mark
        // the entity as "processed" so that we don't do
        // all of this again for the same entity the next
        // time it is loaded into the world.
        NBTUtil.markEntityProcessed(livingEntity);
    }

    /**
     * Modifying final damage dealt to entities by
     * the mobs we have that have a minimum
     * amount of damage they should inflict.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingEntityDamaged(LivingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();

        if (attacker != null) {
            float damage = event.getAmount();

            if (attacker.getType() == ApocalypseEntities.GHOST.get()) {
                event.setAmount(Math.max(1.0F, damage));
            }
            else if (attacker.getType() == ApocalypseEntities.GRUMP.get()) {
                event.setAmount(Math.max(2.0F, damage));
            }
        }
    }

    /**
     * Toast!!!!!!!!
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            Item item = itemEntity.getItem().getItem();

            if (item == Items.BREAD) {
                Level level = event.getEntity().getCommandSenderWorld();
                int itemCount = itemEntity.getItem().getCount();
                ItemStack stack = new ItemStack(ApocalypseItems.FATHERLY_TOAST.get(), itemCount);
                // Toast level, nice
                stack.getOrCreateTag().putInt("ToastLevel", event.getEntity().level().random.nextInt(99) + 1);
                level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), stack));
                itemEntity.discard();
            }
            else if (item == ApocalypseItems.FATHERLY_TOAST.get())
                event.setCanceled(true);
        }
    }

    public static void refreshMobDifficulties() {
        MOB_DIFFICULTIES.clear();
        CommentedConfig config = ApocalypseCommonConfig.COMMON.getMobDifficulties();

        for (CommentedConfig.Entry entry : config.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!StringUtils.isNumeric(value)) {
                logError("Invalid mob difficulty entry \"{}\" found. A mob difficulty entry value must be a number representing the target difficulty level");
                continue;
            }
            ResourceLocation entityId = ResourceLocation.tryParse(key);
            double difficulty = Double.parseDouble(value);

            if (difficulty <= 0) {
                logError("Invalid mob difficulty entry \"{}\" found. The mob difficulty entry value must be a number greater than or equal to 0.");
                continue;
            }
            if (entityId == null) {
                logError("Invalid mob difficulty for entry \"{}\" found. Entry key must be the registry ID of the desired entity type.", key);
                continue;
            }
            EntityType<?> entityType;

            if (ForgeRegistries.ENTITY_TYPES.containsKey(entityId)) {
                entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
            }
            else {
                logError("Found mob difficulty entry with a entity ID that does not exist in the registry: {}. This mob difficulty entry will not be loaded.", entityId);
                continue;
            }

            if (MOB_DIFFICULTIES.containsKey(entityType)) {
                logWarning("Duplicate mob difficulty entry found for entity ID {}, skipping");
            }
            else {
                MOB_DIFFICULTIES.put(entityType, difficulty);
            }
        }
    }

    private static void logError(String message, Object... args) {
        Apocalypse.LOGGER.error("[Apocalypse Config] " + message, args);
    }

    private static void logWarning(String message, Object... args) {
        Apocalypse.LOGGER.warn("[Apocalypse Config] " + message, args);
    }
}
