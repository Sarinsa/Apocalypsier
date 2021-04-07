package com.toast.apocalypse.api.impl;

import com.toast.apocalypse.api.IConfigHelper;
import com.toast.apocalypse.common.core.config.ApocalypseCommonConfig;

public final class ConfigHelper implements IConfigHelper {


    public ConfigHelper() {

    }

    @Override
    public boolean rainDamageEnabled() {
        return ApocalypseCommonConfig.COMMON.rainDamageEnabled();
    }
}
