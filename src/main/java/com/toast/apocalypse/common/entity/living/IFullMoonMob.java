package com.toast.apocalypse.common.entity.living;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.core.config.ApocalypseCommonConfig;
import com.toast.apocalypse.common.core.mod_event.events.AbstractEvent;
import com.toast.apocalypse.common.core.register.ApocalypseParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/** Represents a mob type that spawns during full moons */
public interface IFullMoonMob {

    /** Key used for storing the full moon mob's player target UUID to NBT. */
    String PLAYER_UUID_KEY = "PlayerTargetUUID";
    /** Key used for storing the full moon mob's event generation to NBT. */
    String EVENT_GEN_KEY = "EventGeneration";

    /**
     * @return The UUID of this full moon mob's set
     *         player target. Full moon mobs spawned
     *         from commands or spawn eggs will normally
     *         not have a target UUID, and may return null.
     */
    @Nullable
    UUID getPlayerTargetUUID();

    /**
     * @return The "generation" this full moon mob belongs to.<br>
     * <br>
     * When the event starts, we are at generation 0.
     * When the player dies, we move onto the next generation,
     * and the full moon mobs that have already spawned that belong to the older
     * generation, should be removed.
     */
    int getEventGeneration();

    /**
     * Sets the event generation value for this full moon mob.
     */
    void setEventGeneration(int generation);

    /**
     * Sets this full moon mob's target UUID.<br>
     * The target UUID is the UUID of the player
     * this full moon mob was spawned for, if spawned
     * from a full moon siege event.<br>
     * <br>
     *
     * @param playerTargetUUID The UUID of the specified player to target.
     */
    void setPlayerTargetUUID(@Nullable UUID playerTargetUUID);

    @Nullable
    static <E extends LivingEntity & IFullMoonMob> Player getEventTarget(E moonMob) {
        if (moonMob.getPlayerTargetUUID() != null) {
            return moonMob.level().getPlayerByUUID(moonMob.getPlayerTargetUUID());
        }
        return null;
    }

    /**
     * This is a bit weird to explain, but here goes!<br>
     * <br>
     * When the player dies, their <strong>"event generation"</strong> increments. Full moon mobs
     * stores the value of what the event generation was when they spawned. This method
     * checks if the full moon mob's stored value is <strong>different</strong> from the player's current
     * event generation, in which case it should despawn.
     */
    static boolean shouldDisappear(@Nullable UUID playerTargetUUID, ServerLevel level, IFullMoonMob moonMob) {
        if (!ApocalypseCommonConfig.COMMON.getDespawnMobsOnDeath())
            return false;

        if (playerTargetUUID == null)
            return false;

        ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerTargetUUID);

        // Player might be offline, do nothing
        if (player == null)
            return false;

        AbstractEvent event = Apocalypse.INSTANCE.getDifficultyManager().getCurrentEvent(player);

        if (event != null) {
            final int generation = event.getEventGeneration();
            return moonMob.getEventGeneration() != generation;
        }
        return false;
    }

    static void spawnSmoke(ServerLevel level, Mob mob) {
        for (int i = 0; i < 8; i++) {
            level.sendParticles(ApocalypseParticles.LUNAR_DESPAWN_SMOKE.get(), mob.getX(), mob.getY(), mob.getZ(), 4, 0.1, 0.1, 0.1, 0.1);
        }
    }
}
