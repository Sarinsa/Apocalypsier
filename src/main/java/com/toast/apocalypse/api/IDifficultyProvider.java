package com.toast.apocalypse.api;

/**
 * A simple interface that lets modders interact
 * with Apocalypse's difficulty manager to a
 * limited degree.
 */
public interface IDifficultyProvider {

    /**
     * @return The current world difficulty.
     */
    long getDifficulty();

    /**
     * @return The current world difficulty rate.
     */
    double getDifficultyRate();

    /**
     * @return The ID of the currently running
     *         event, if any.
     *
     * No event: -1
     *
     * Full moon: 0
     *
     * Thunderstorm: 1
     */
    int currentEventId();
}
