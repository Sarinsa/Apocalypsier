package com.toast.apocalypse.api.impl;

import com.toast.apocalypse.api.plugin.IApocalypseApi;
import com.toast.apocalypse.api.plugin.DifficultyProvider;
import com.toast.apocalypse.api.plugin.RegistryHelper;
import com.toast.apocalypse.common.core.Apocalypse;

public final class ApocalypseAPI implements IApocalypseApi {

    private final DifficultyProvider difficultyProvider;

    public ApocalypseAPI() {
        this.difficultyProvider = new DifficultyProviderImpl();
    }

    @Override
    public RegistryHelper getRegistryHelper() {
        return Apocalypse.INSTANCE.getRegistryHelper();
    }

    @Override
    public DifficultyProvider getDifficultyProvider() {
        return this.difficultyProvider;
    }
}
