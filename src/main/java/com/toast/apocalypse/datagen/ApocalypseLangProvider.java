package com.toast.apocalypse.datagen;

import com.toast.apocalypse.common.core.Apocalypse;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class ApocalypseLangProvider extends LanguageProvider {

    public ApocalypseLangProvider(DataGenerator gen) {
        super(gen.getPackOutput(), Apocalypse.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
    }
}
