package com.toast.apocalypse.api.plugin;

import java.util.List;

/**
 * An easy way for modders to
 * check Apocalypse's config
 * settings. Note that this
 * part will likely whenever
 * we update the config internally.
 */
public interface IConfigHelper {

    /**
     * @return True if rain damage is
     *         enabled in the mod config.
     */
    boolean rainDamageEnabled();

    /**
     * @return The amount of damage that rain
     *         inflicts per rain damage tick.
     */
    float rainDamageAmount();

    /**
     * @return A list of Strings representing the registry
     *         names of the worlds that are configured to
     *         give an increased difficulty multiplier whenever
     *         a player is in any of these dimensions.
     */
    List<? extends String> penaltyDimensions();
}
