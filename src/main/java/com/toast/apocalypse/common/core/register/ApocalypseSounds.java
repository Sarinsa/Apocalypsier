package com.toast.apocalypse.common.core.register;

import com.toast.apocalypse.common.core.Apocalypse;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ApocalypseSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Apocalypse.MODID);


    public static final RegistryObject<SoundEvent> GHOST_FREEZE = register("entity.ghost.ghost_freeze");
    public static final RegistryObject<SoundEvent> GRUMP_RAGE = register("entity.grump.rage");


    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Apocalypse.resourceLoc(name)));
    }
}
