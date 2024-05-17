package com.toast.apocalypse.common.core.difficulty;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.core.config.CommonConfigReloadListener;
import com.toast.apocalypse.common.core.config.util.ServerConfigHelper;
import com.toast.apocalypse.common.core.mod_event.EventRegistry;
import com.toast.apocalypse.common.core.mod_event.EventType;
import com.toast.apocalypse.common.core.mod_event.events.AbstractEvent;
import com.toast.apocalypse.common.network.NetworkHelper;
import com.toast.apocalypse.common.network.message.S2CSimpleClientTask;
import com.toast.apocalypse.common.triggers.ApocalypseTriggers;
import com.toast.apocalypse.common.util.CapabilityHelper;
import com.toast.apocalypse.common.util.RainDamageTickHelper;
import com.toast.apocalypse.common.util.References;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * This class manages player difficulty and mod events
 * like the full moon siege and thunderstorm.
 */
public final class PlayerDifficultyManager {

    /** These are updated when the mod config is loaded/reloaded<br>
     * <br>
     *
     *  @see CommonConfigReloadListener#updateInfo()
     */
    public static double ACID_RAIN_CHANCE;
    public static boolean MULTIPLAYER_DIFFICULTY_SCALING;
    public static double MULTIPLAYER_DIFFICULTY_MULT;
    public static double SLEEP_PENALTY;
    public static double DIMENSION_PENALTY;
    public static List<ResourceKey<Level>> DIMENSION_PENALTY_LIST;

    /** Number of ticks per update. */
    public static final int TICKS_PER_UPDATE = 5;
    /** Number of ticks per save. */
    public static final int TICKS_PER_SAVE = 160;
    /** Number of ticks per advancement trigger check. */
    public static final int TICKS_PER_ADV_CHECK = 200;

    /** Time until next server tick update. */
    private int timeUpdate = 0;
    /** Time until next save. */
    private int timeSave = 0;
    /** Time until next advancement trigger check. */
    private int timeAdvCheck = 0;

    /** Contains miscellaneous info about each world. */
    private final Map<Level, WorldInfo> worldInfo = new HashMap<>();

    /** A Map containing each online player's current event. */
    private final Map<UUID, AbstractEvent> playerEvents = new HashMap<>();

    /** Manages rain damage. */
    private final RainDamageTickHelper rainDamageHelper;

    /** Server instance. */
    private MinecraftServer server;

    /** Whether the current server instance has been shut down. */
    private boolean serverStopped = false;


    public PlayerDifficultyManager() {
        rainDamageHelper = new RainDamageTickHelper();
    }

    public static long queryDayTime(long dayTime) {
        return dayTime % References.DAY_LENGTH;
    }

    /**
     * Used to find the difficulty of the nearest player when
     * calculating mob attribute bonuses and equipment etc.<br>
     * <br>
     *
     * @param level The World :)
     * @param livingEntity The entity to use as reference point.<br>
     * <br>
     * @return The unscaled, raw difficulty of the nearest player.
     *         Defaults to 0 if no player can be found.
     */
    public static long getNearestPlayerDifficulty(LevelAccessor level, LivingEntity livingEntity) {
        Player player = level.getNearestPlayer(livingEntity, Double.MAX_VALUE);

        if (player != null) {
            return CapabilityHelper.getPlayerDifficulty(player);
        }
        return 0;
    }

    public static long getNearestPlayerDifficulty(LevelAccessor level, BlockPos pos) {
        Player player = level.getNearestPlayer(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, Double.MAX_VALUE, false);

        if (player != null) {
            return CapabilityHelper.getPlayerDifficulty(player);
        }
        return 0;
    }

    public boolean isFullMoon() {
        ServerLevel world = server.overworld();
        return world.dimensionType().moonPhase(world.getDayTime()) == 0;
    }

    public boolean isFullMoonNight() {
        ServerLevel world = server.overworld();
        long dayTime = queryDayTime(world.getDayTime());

        return isFullMoon() && dayTime > 13000L && dayTime < 23500L;
    }

    public boolean isRainingAcid(ServerLevel world) {
        return worldInfo.get(world).isRainingAcid();
    }

    /** Fetch the server instance and update integrated server mod server config. */
    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        server = event.getServer();
        if (!server.isDedicatedServer()) {
            ServerConfigHelper.updateModServerConfig();
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        serverStopped = false;
        event.getServer().getAllLevels().forEach((world) -> worldInfo.put(world, new WorldInfo(world)));
    }

    /** Clean up references and save player event data */
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            saveEventData(player);
        }
        cleanup();
        serverStopped = true;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().getCommandSenderWorld().isClientSide) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            ServerLevel overworld = server.overworld();
            ServerLevel playerLevel = player.serverLevel();

            NetworkHelper.sendUpdatePlayerDifficulty(player);
            NetworkHelper.sendUpdatePlayerDifficultyMult(player);
            NetworkHelper.sendUpdatePlayerMaxDifficulty(player);
            // TODO - Dunno when this will become reality
            //NetworkHelper.sendMobWikiIndexUpdate(player);
            NetworkHelper.sendMoonPhaseUpdate(player, overworld);
            NetworkHelper.sendSimpleClientTaskRequest(player, isRainingAcid(playerLevel) ? S2CSimpleClientTask.SET_ACID_RAIN : S2CSimpleClientTask.REMOVE_ACID_RAIN);

            loadEventData(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // Don't bother saving event data
        // if the server has already been stopped
        // as it will have been taken care of already.
        if (serverStopped) return;

        if (!event.getEntity().getCommandSenderWorld().isClientSide) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            saveEventData(player);
            playerEvents.get(player.getUUID()).stop(player.serverLevel());
            playerEvents.remove(player.getUUID());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            playerEvents.get(serverPlayer.getUUID()).onPlayerDeath(serverPlayer, serverPlayer.serverLevel());
        }
    }

    /**
     * Called each game tick to update all players'
     * difficulty properties and Apocalypse events.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MinecraftServer server = this.server;

            rainDamageHelper.checkAndPerformRainDamageTick(server.getAllLevels(), this);

            // Update player difficulty and event
            if (++timeUpdate >= TICKS_PER_UPDATE) {
                timeUpdate = 0;

                // Update player difficulty and event
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    updatePlayerDifficulty(player);
                    updatePlayerEvent(player);
                }

                // Update world info
                for (ServerLevel level : server.getAllLevels()) {
                    WorldInfo info = worldInfo.get(level);

                    if (level.isRaining()) {
                        if (!info.justStartedRaining()) {
                            info.setJustStartedRaining(true, level.random);
                        }
                    }
                    else {
                        info.setJustStartedRaining(false, level.random);
                        info.setRainingAcid(false);
                    }
                }
            }

            // Save event data
            if (++timeSave >= TICKS_PER_SAVE) {
                timeSave = 0;

                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    saveEventData(player);
                    // Cheekily sneak in a moon phase update here, since
                    // it doesn't exactly need to happen often.
                    NetworkHelper.sendMoonPhaseUpdate(player, server.overworld());
                }
            }

            // Check if players have passed their grace
            // period and grant the base achievement if so.
            if (++timeAdvCheck >= TICKS_PER_ADV_CHECK) {
                timeAdvCheck = 0;

                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    ApocalypseTriggers.PASSED_GRACE_PERIOD.trigger(player, CapabilityHelper.getPlayerDifficulty(player));
                }
            }
        }
    }


    /**
     * Updates the player's difficulty.
     */
    private void updatePlayerDifficulty(ServerPlayer player) {
        final int playerCount = server.getPlayerCount();
        final long maxDifficulty = CapabilityHelper.getMaxPlayerDifficulty(player);
        double difficultyMultiplier = CapabilityHelper.getPlayerDifficultyMult(player);
        long currentDifficulty = CapabilityHelper.getPlayerDifficulty(player);

        // Apply multiplayer difficulty multiplier, if enabled.
        if (MULTIPLAYER_DIFFICULTY_SCALING) {
            difficultyMultiplier = 1.0D;

            if (playerCount > 1) {
                difficultyMultiplier += ((playerCount - 1.0D) * MULTIPLAYER_DIFFICULTY_MULT);
            }
        }

        // Apply dimension difficulty rate penalty if any player is in a dimension marked for penalty
        if (DIMENSION_PENALTY > 0.0D) {
            if (!player.isSpectator() && DIMENSION_PENALTY_LIST.contains(player.getCommandSenderWorld().dimension())) {
                difficultyMultiplier *= 1.0 + DIMENSION_PENALTY;
            }
        }
        boolean maxDifficultyReached = maxDifficulty >= 0 && currentDifficulty >= maxDifficulty;

        if (maxDifficultyReached || player.isCreative() || player.isSpectator()) {
            return;
        }
        currentDifficulty += TICKS_PER_UPDATE * difficultyMultiplier;

        // Update player difficulty stuff
        CapabilityHelper.setPlayerDifficulty(player, currentDifficulty);
        CapabilityHelper.setPlayerDifficultyMult(player, difficultyMultiplier);
    }

    /**
     * Updates the given player's current event
     * and checks what event should be active.
     *
     * @param player The player to update event for.
     */
    @SuppressWarnings("ConstantConditions")
    public void updatePlayerEvent(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ServerLevel overworld = server.overworld();
        AbstractEvent currentEvent = getCurrentEvent(player);

        // This should never happen, and it would be super weird if it did
        if (currentEvent == null)
            return;

        EventType<?> eventType = currentEvent.getType();

        // Update current event
        currentEvent.update(level, player, this);

        if (CapabilityHelper.getPlayerDifficulty(player) > 0 && overworld.getGameTime() > 0L) {
            for (EventType<?> type : EventRegistry.EVENTS.values()) {
                if (eventType != type && type.getStartPredicate().test(level, eventType, player, this) && type.getPriority() > currentEvent.getType().getPriority()) {
                    // Copy over event generation
                    int generation = currentEvent.getEventGeneration();
                    eventType = startEvent(player, currentEvent, type);
                    getCurrentEvent(player).setEventGeneration(generation);
                    break;
                }
            }
            if (!eventType.getPersistPredicate().test(level, eventType, player, this)) {
                endEvent(player);
            }
        }
    }

    /** Starts an event for the given player, if possible.
     *
     * @param player The player to start the event for.
     * @param eventType The event type for the event to start.
     */
    public EventType<?> startEvent(ServerPlayer player, AbstractEvent currentEvent, @Nonnull EventType<?> eventType) {
        currentEvent.onEnd(server, player);
        AbstractEvent newEvent = eventType.createEvent();
        newEvent.onStart(server, player);
        playerEvents.put(player.getUUID(), newEvent);
        saveEventData(player);

        if (eventType.getEventStartMessage() != null) {
            player.displayClientMessage(Component.translatable(eventType.getEventStartMessage()), true);
        }
        return eventType;
    }

    public int getEventId(ServerPlayer player) {
        UUID uuid = player.getUUID();
        return playerEvents.containsKey(uuid) ? playerEvents.get(uuid).getType().getId() : -1;
    }

    // SHOULD not return null, but who knows
    @Nullable
    public AbstractEvent getCurrentEvent(ServerPlayer player) {
        UUID uuid = player.getUUID();
        return playerEvents.getOrDefault(uuid, null);
    }

    /** Ends the current active event, if any. */
    public void endEvent(ServerPlayer player) {
        UUID uuid = player.getUUID();
        playerEvents.get(uuid).onEnd(server, player);
        playerEvents.put(uuid, EventRegistry.NONE.createEvent());
        saveEventData(player);
    }

    /** Cleans up the references to things in a server when the server stops. */
    public void cleanup() {
        server = null;
        timeUpdate = 0;
        timeSave = 0;
        timeAdvCheck = 0;
        playerEvents.clear();
        rainDamageHelper.resetTimer();
        worldInfo.clear();
    }

    /** Loads the given player's event data. */
    public void loadEventData(ServerPlayer player) {
        try {
            AbstractEvent currentEvent = EventRegistry.NONE.createEvent();
            CompoundTag eventData = CapabilityHelper.getEventData(player);

            if (eventData != null && eventData.contains("EventId", Tag.TAG_INT)) {
                currentEvent = EventRegistry.getFromId(eventData.getInt("EventId")).createEvent();
                currentEvent.read(eventData, player, player.serverLevel());
            }
            playerEvents.put(player.getUUID(), currentEvent);
        }
        catch (Exception e) {
            logError("Failed to read world save data for player " + player.getName().getString() + ". That shouldn't happen.");
            e.printStackTrace();
        }
    }

    /** Saves the data of the player's current event. */
    public void saveEventData(ServerPlayer player) {
        try {
            if (playerEvents.containsKey(player.getUUID())) {
                AbstractEvent currentEvent = playerEvents.get(player.getUUID());
                CompoundTag eventData = new CompoundTag();

                currentEvent.write(eventData);
                CapabilityHelper.setEventData(player, eventData);
            }
            else {
                logError("No event object found for player " + player.getName().getString() + ". Not good!");
            }
        }
        catch (Exception e) {
            logError("Failed to write player event data for player " + player.getName().getString() + "! Not cool beans.");
            e.printStackTrace();
        }
    }

    /** Helper method for logging. */
    private static void logInfo(String message) {
        Apocalypse.LOGGER.log(org.apache.logging.log4j.Level.INFO, "[{}] " + message, PlayerDifficultyManager.class.getSimpleName());
    }

    /** Helper method for logging. */
    private static void logError(String message) {
        Apocalypse.LOGGER.log(org.apache.logging.log4j.Level.ERROR, "[{}] " + message, PlayerDifficultyManager.class.getSimpleName());
    }


    /** Contains miscellaneous info about a world. */
    public static class WorldInfo {

        protected static final String saveDataId = Apocalypse.resourceLoc("world_info").toString();

        private final ServerLevel level;
        private final WorldInfoSavedData savedData;

        private boolean justStartedRaining;
        private boolean isRainingAcid;


        private WorldInfo(ServerLevel level) {
            this.level = level;
            this.savedData = level.getDataStorage().computeIfAbsent(this::load, this::create, saveDataId);
        }

        protected void setRainingAcid(boolean value) {
            isRainingAcid = value;

            savedData.setDirty();

            for (ServerPlayer player : level.players()) {
                NetworkHelper.sendSimpleClientTaskRequest(player, value ? S2CSimpleClientTask.SET_ACID_RAIN : S2CSimpleClientTask.REMOVE_ACID_RAIN);
            }
        }

        public boolean isRainingAcid() {
            return isRainingAcid;
        }

        public boolean justStartedRaining() {
            return justStartedRaining;
        }

        public void setJustStartedRaining(boolean value, RandomSource random) {
            justStartedRaining = value;

            if (value && random.nextDouble() <= ACID_RAIN_CHANCE)
                setRainingAcid(true);
        }

        public WorldInfoSavedData load(CompoundTag compoundNBT) {
            WorldInfoSavedData savedData = new WorldInfoSavedData(this);

            if (compoundNBT.contains("RainingAcid", Tag.TAG_BYTE)) {
                isRainingAcid = compoundNBT.getBoolean("RainingAcid");
            }
            return savedData;
        }

        public WorldInfoSavedData create() {
            return new WorldInfoSavedData(this);
        }

        protected static class WorldInfoSavedData extends SavedData {

            private final WorldInfo worldInfo;

            public WorldInfoSavedData(WorldInfo worldInfo) {
                this.worldInfo = worldInfo;
            }

            @Override
            public CompoundTag save(CompoundTag compoundNBT) {
                compoundNBT.putBoolean("RainingAcid", worldInfo.isRainingAcid);
                return compoundNBT;
            }
        }
    }
}
