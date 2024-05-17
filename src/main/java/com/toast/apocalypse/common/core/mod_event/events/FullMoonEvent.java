package com.toast.apocalypse.common.core.mod_event.events;

import com.toast.apocalypse.common.core.difficulty.PlayerDifficultyManager;
import com.toast.apocalypse.common.core.mod_event.EventType;
import com.toast.apocalypse.common.core.register.ApocalypseEntities;
import com.toast.apocalypse.common.entity.living.IFullMoonMob;
import com.toast.apocalypse.common.tag.ApocalypseEntityTags;
import com.toast.apocalypse.common.util.CapabilityHelper;
import com.toast.apocalypse.common.util.DataStructureUtils;
import com.toast.apocalypse.common.util.References;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.pathfinder.PathComputationType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Random;

/**
 * The full moon event. This event can occur every 8 days in game and will interrupt any other event and can not be interrupted
 * by any other event.<br>
 * These are often referred to as "full moon sieges" in other parts of the code and in the properties file.
 */
public final class FullMoonEvent extends AbstractEvent {

    /** The time it takes from when the event is triggered
     *  until siege mobs can start spawning.
     */
    private static final int MAX_GRACE_PERIOD = 800;

    /** The difficulty until mob counts increase */
    public static double MOB_COUNT_TIME_SPAN;

    /** The starting difficulty for when the various full moon mobs can start spawning */
    public static double GHOST_START;
    public static double BREECHER_START;
    public static double GRUMP_START;
    public static double SEEKER_START;
    public static double DESTROYER_START;

    /** The minimum amount of each mob type to be spawned (depends on difficulty) */
    public static int GHOST_MIN_COUNT;
    public static int BREECHER_MIN_COUNT;
    public static int GRUMP_MIN_COUNT;
    public static int SEEKER_MIN_COUNT;
    public static int DESTROYER_MIN_COUNT;

    /** Additional mob counts for each mob type scaled with difficulty */
    public static double GHOST_ADDITIONAL_COUNT;
    public static double BREECHER_ADDITIONAL_COUNT;
    public static double GRUMP_ADDITIONAL_COUNT;
    public static double SEEKER_ADDITIONAL_COUNT;
    public static double DESTROYER_ADDITIONAL_COUNT;

    /** The maximum amount of each mob type that can spawn */
    public static int GHOST_MAX_COUNT;
    public static int BREECHER_MAX_COUNT;
    public static int GRUMP_MAX_COUNT;
    public static int SEEKER_MAX_COUNT;
    public static int DESTROYER_MAX_COUNT;


    /** Numeric IDs representing each full moon mob */
    private static final int GHOST_ID = 0;
    private static final int BREECHER_ID = 1;
    private static final int GRUMP_ID = 2;
    private static final int SEEKER_ID = 3;
    private static final int DESTROYER_ID = 4;

    /** Time until mobs can start spawning. */
    private int gracePeriod;

    /** The time between each mob spawn */
    private int spawnTime = 600;

    /** The time until the next mob should be spawned for the player */
    private int timeUntilNextSpawn = 0;

    /** Whether there are any mobs left to spawn */
    private boolean hasMobsLeft = true;

    /** A map containing all the full moon mobs that will be spawned for the player */
    private final HashMap<Integer, Integer> mobsToSpawn = new HashMap<>();

    public FullMoonEvent(EventType<?> type) {
        super(type);
    }

    @Override
    public void onStart(MinecraftServer server, ServerPlayer player) {
        long difficulty = CapabilityHelper.getPlayerDifficulty(player);
        calculateMobs(difficulty);
        calculateSpawnTime();
        gracePeriod = MAX_GRACE_PERIOD;
    }

    @Override
    public void update(ServerLevel level, ServerPlayer player, PlayerDifficultyManager difficultyManager) {
        // Tick grace period
        if (gracePeriod > 0) {
            gracePeriod -= PlayerDifficultyManager.TICKS_PER_UPDATE;
        }
        // Tick time until next mob spawn
        if (timeUntilNextSpawn > 0) {
            timeUntilNextSpawn -= PlayerDifficultyManager.TICKS_PER_UPDATE;
        }

        if (canSpawn()) {
            boolean hasMobsLeft = false;

            for (int id : mobsToSpawn.keySet()) {
                if (mobsToSpawn.get(id) > 0) {
                    hasMobsLeft = true;
                    break;
                }
            }
            this.hasMobsLeft = hasMobsLeft;

            if (!hasMobsLeft)
                return;

            RandomSource random = level.getRandom();
            int mobIndex = getRandomMobIndex(random);

            if (mobIndex < 0)
                return;

            int currentCount = mobsToSpawn.get(mobIndex);
            mobsToSpawn.put(mobIndex, --currentCount);

            if (level.getDifficulty() != Difficulty.PEACEFUL) {
                spawnMobFromIndex(mobIndex, level, player);
            }
            timeUntilNextSpawn = spawnTime;
        }
    }

    @Override
    public void onEnd(MinecraftServer server, ServerPlayer player) {

    }

    @Override
    public void stop(ServerLevel level) {}

    /**
     * Returns true if it is time to spawn a new full moon mob.
     */
    private boolean canSpawn() {
        return gracePeriod <= 0 && hasMobsLeft && timeUntilNextSpawn <= 0;
    }

    /**
     * Calculates the amount of full moon mobs that should be spawned for this even's player.
     *
     * @param difficulty The player's difficulty.
     */
    private void calculateMobs(long difficulty) {
        final double scaledDifficulty = (double) difficulty / References.DAY_LENGTH;
        double effectiveDifficulty;
        int count;

        // Ensure nothing is null before proceeding.
        mobsToSpawn.put(GHOST_ID, 0);
        mobsToSpawn.put(BREECHER_ID, 0);
        mobsToSpawn.put(GRUMP_ID, 0);
        mobsToSpawn.put(SEEKER_ID, 0);
        mobsToSpawn.put(DESTROYER_ID, 0);

        if (GHOST_START >= 0L && GHOST_START <= scaledDifficulty) {
            effectiveDifficulty = (scaledDifficulty - GHOST_START) / MOB_COUNT_TIME_SPAN;
            count = GHOST_MIN_COUNT + (int) (GHOST_ADDITIONAL_COUNT * effectiveDifficulty);
            mobsToSpawn.put(GHOST_ID, Math.min(count, GHOST_MAX_COUNT));
        }
        if (BREECHER_START >= 0L && BREECHER_START <= scaledDifficulty) {
            effectiveDifficulty = (scaledDifficulty - BREECHER_START) / MOB_COUNT_TIME_SPAN;
            count = BREECHER_MIN_COUNT + (int) (BREECHER_ADDITIONAL_COUNT * effectiveDifficulty);
            mobsToSpawn.put(BREECHER_ID, Math.min(count, BREECHER_MAX_COUNT));
        }
        if (GRUMP_START >= 0L && GRUMP_START <= scaledDifficulty) {
            effectiveDifficulty = (scaledDifficulty - GRUMP_START) / MOB_COUNT_TIME_SPAN;
            count = GRUMP_MIN_COUNT + (int) (GRUMP_ADDITIONAL_COUNT * effectiveDifficulty);
            mobsToSpawn.put(GRUMP_ID, Math.min(count, GRUMP_MAX_COUNT));
        }
        if (SEEKER_START >= 0L && SEEKER_START <= scaledDifficulty) {
            effectiveDifficulty = (scaledDifficulty - SEEKER_START) / MOB_COUNT_TIME_SPAN;
            count = SEEKER_MIN_COUNT + (int) (SEEKER_ADDITIONAL_COUNT * effectiveDifficulty);
            mobsToSpawn.put(SEEKER_ID, Math.min(count, SEEKER_MAX_COUNT));
        }
        if (DESTROYER_START >= 0L && DESTROYER_START <= scaledDifficulty) {
            effectiveDifficulty = (scaledDifficulty - DESTROYER_START) / MOB_COUNT_TIME_SPAN;
            count = DESTROYER_MIN_COUNT + (int) (DESTROYER_ADDITIONAL_COUNT * effectiveDifficulty);
            mobsToSpawn.put(DESTROYER_ID, Math.min(count, DESTROYER_MAX_COUNT));
        }
    }

    /** Calculates the interval between each mob spawn */
    private void calculateSpawnTime() {
        final int defaultSpawnTime = 500;
        int totalMobCount = 0;

        for (int mobId : mobsToSpawn.keySet()) {
            totalMobCount += mobsToSpawn.get(mobId);
        }
        this.spawnTime = totalMobCount <= 0 ? defaultSpawnTime : (10500 - MAX_GRACE_PERIOD) / totalMobCount;
    }

    /**
     * Returns a random mob index for the mob types remaining,
     * or -1 if there are no mob types left to spawn.
     */
    private int getRandomMobIndex(RandomSource random) {
        Integer type = DataStructureUtils.randomMapKeyFiltered(random, mobsToSpawn, (id, count) -> count > 0);
        return type != null ? type : -1;
    }

    /**
     * Spawns a full moon mob. The type of mob depends on the mob index given.
     *
     * @param mobType The index of what type of mob to spawn.
     * @param level The world to spawn this mob in.
     * @param player The player to spawn this mob for.
     */
    private void spawnMobFromIndex(int mobType, ServerLevel level, ServerPlayer player) {
        Mob mob = switch (mobType) {
            case GHOST_ID -> spawnMob(ApocalypseEntities.GHOST.get(), player, level);
            case BREECHER_ID -> spawnMob(ApocalypseEntities.BREECHER.get(), player, level);
            case GRUMP_ID -> spawnMob(ApocalypseEntities.GRUMP.get(), player, level);
            case SEEKER_ID -> spawnMob(ApocalypseEntities.SEEKER.get(), player, level);
            case DESTROYER_ID -> spawnMob(ApocalypseEntities.DESTROYER.get(), player, level);
            default -> null;
        };
        if (mob == null)
            return;

        ((IFullMoonMob) mob).setPlayerTargetUUID(player.getUUID());
        ((IFullMoonMob) mob).setEventGeneration(getEventGeneration());
        level.addFreshEntity(mob);
    }

    @Nullable
    private <T extends Mob & IFullMoonMob> T spawnMob(EntityType<T> entityType, ServerPlayer player, ServerLevel level) {
        RandomSource random = level.getRandom();
        BlockPos playerPos = player.blockPosition();
        BlockPos spawnPos = null;
        final int minDist = 25;

        // The ghost is a special case, and we don't need to care about placement at all really
        if (entityType == ApocalypseEntities.GHOST.get()) {
            BlockPos pos;

            for (int i = 0; i < 10; i++) {
                int startX = random.nextBoolean() ? minDist : -minDist;
                int startZ = random.nextBoolean() ? minDist : -minDist;
                int x = playerPos.getX() + startX + (startX < 1 ? -random.nextInt(40) : random.nextInt(40));
                int z = playerPos.getZ() + startZ + (startZ < 1 ? -random.nextInt(40) : random.nextInt(40));
                pos = new BlockPos(x, 20 + random.nextInt(60), z);

                if (level.isLoaded(pos)) {
                    spawnPos = pos;
                    break;
                }
            }
        }
        else {
            SpawnPlacements.Type placementType = SpawnPlacements.getPlacementType(entityType);

            for (int tries = 0; tries < 10; tries++) {
                int startX = random.nextBoolean() ? minDist : -minDist;
                int startZ = random.nextBoolean() ? minDist : -minDist;
                int x = playerPos.getX() + startX + (startX < 1 ? -random.nextInt(46) : random.nextInt(46));
                int z = playerPos.getZ() + startZ + (startZ < 1 ? -random.nextInt(46) : random.nextInt(46));
                int y = level.getHeight(SpawnPlacements.getHeightmapType(entityType), x, z);
                BlockPos.MutableBlockPos pos = new BlockPos(x, y, z).mutable();

                if (level.dimensionType().hasCeiling()) {
                    do {
                        pos.move(Direction.DOWN);
                    }
                    while(!level.getBlockState(pos).isAir());

                    do {
                        pos.move(Direction.DOWN);
                    }
                    while(level.getBlockState(pos).isAir() && pos.getY() > 0);
                }
                if (placementType == SpawnPlacements.Type.ON_GROUND) {
                    BlockPos blockpos = pos.below();
                    if (level.getBlockState(blockpos).isPathfindable(level, blockpos, PathComputationType.LAND)) {
                        pos = blockpos.mutable();
                    }
                }
                if (level.isLoaded(pos) && NaturalSpawner.isSpawnPositionOk(placementType, level, pos, entityType)) {
                    // We check for collisions around the entity and a bit above if we are dealing with a flying
                    // entity, so we can spawn it in the air a bit above ground to help prevent them getting stuck in the ground.
                    if (isFlyingType(entityType)) {
                        pos = pos.above(20).mutable();

                        if (level.noCollision(entityType.getAABB((double) pos.getX() + 0.5D, pos.getY(), (double) pos.getZ() + 0.5D)
                                .inflate(0.0D, 2.0D, 0.5D)
                                .move(0.0D, 2.0D, 0.0D))) {
                            spawnPos = pos.immutable();
                            break;
                        }
                    }
                    else {
                        if (level.noCollision(entityType.getAABB((double) pos.getX() + 0.5D, pos.getY(), (double) pos.getZ() + 0.5D))) {
                            spawnPos = pos.immutable();
                            break;
                        }
                    }
                }
            }
        }
        if (spawnPos == null)
            return null;

        return entityType.create(level, null, null, spawnPos, MobSpawnType.EVENT, true, true);
    }

    private static boolean isFlyingType(EntityType<?> entityType) {
        return entityType.is(ApocalypseEntityTags.FLYING_ENTITIES);
    }

    @Override
    public void writeAdditional(CompoundTag data) {
        data.putInt("GracePeriod", gracePeriod);
        data.putInt("TimeNextSpawn", timeUntilNextSpawn);
        data.putInt("SpawnTime", spawnTime);
        data.putInt("EventGeneration", eventGeneration);

        CompoundTag mobsToSpawn = new CompoundTag();
        mobsToSpawn.putInt("Ghost", this.mobsToSpawn.getOrDefault(GHOST_ID, 0));
        mobsToSpawn.putInt("Breecher", this.mobsToSpawn.getOrDefault(BREECHER_ID, 0));
        mobsToSpawn.putInt("Grump", this.mobsToSpawn.getOrDefault(GRUMP_ID, 0));
        mobsToSpawn.putInt("Seeker", this.mobsToSpawn.getOrDefault(SEEKER_ID, 0));
        mobsToSpawn.putInt("Destroyer", this.mobsToSpawn.getOrDefault(DESTROYER_ID, 0));

        data.put("MobsToSpawn", mobsToSpawn);
    }

    @Override
    public void read(CompoundTag data, ServerPlayer player, ServerLevel level) {
        gracePeriod = data.getInt("GracePeriod");
        timeUntilNextSpawn = data.getInt("TimeNextSpawn");
        spawnTime = data.getInt("SpawnTime");
        eventGeneration = data.getInt("EventGeneration");

        CompoundTag mobsToSpawn = data.getCompound("MobsToSpawn");
        this.mobsToSpawn.put(GHOST_ID, mobsToSpawn.getInt("Ghost"));
        this.mobsToSpawn.put(BREECHER_ID, mobsToSpawn.getInt("Breecher"));
        this.mobsToSpawn.put(GRUMP_ID, mobsToSpawn.getInt("Grump"));
        this.mobsToSpawn.put(SEEKER_ID, mobsToSpawn.getInt("Seeker"));
        this.mobsToSpawn.put(DESTROYER_ID, mobsToSpawn.getInt("Destroyer"));
    }
}
