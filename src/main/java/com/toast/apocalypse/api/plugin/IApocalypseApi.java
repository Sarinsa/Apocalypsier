package com.toast.apocalypse.api.plugin;

/**
 * This is the main interface of the APi, containing all the<br>
 * methods for retrieving various helpers and whatnot.
 */
public interface IApocalypseApi {

    /**
     * @return The RegistryHelper instance
     *         parsed by Apocalypse
     */
    RegistryHelper getRegistryHelper();

    /**
     * @return The DifficultyProviderImpl instance
     *         parsed by Apocalypse.
     */
    DifficultyProvider getDifficultyProvider();
}
