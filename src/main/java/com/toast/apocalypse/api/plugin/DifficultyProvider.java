package com.toast.apocalypse.api.plugin;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * A simple interface for reading various
 * difficulty related data from players.
 */
public interface DifficultyProvider {

    /**
     * @return The player's current difficulty rate/multiplier.
     */
    <T extends Player> double getDifficultyRate(T player);

    /**
     * @return The current difficulty of the specified player.
     */
    <T extends Player> long getPlayerDifficulty(T player);

    /**
     * @return The current max difficulty of the specified player.
     */
    <T extends Player> long getMaxPlayerDifficulty(T player);

    /**
     * @return The ID of the given player's
     *         currently running event.
     *         <strong>(Server side only)</strong><br>
     *         <br>
     * No event: -1<br>
     * <br>
     * Full Moon Siege: 0<br>
     * <br>
     * Thunderstorm: 1<br>
     * <br>
     * Acid rain: 2
     */
    <T extends ServerPlayer> int currentEventId(T player);
}
