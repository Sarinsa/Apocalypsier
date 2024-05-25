package com.toast.apocalypse.api.register;

import com.toast.apocalypse.api.BaseTrapAction;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Supplier;

public class ModRegistries {

    /**
     * Created during {@link net.minecraftforge.registries.NewRegistryEvent}.
     */
    public static Supplier<IForgeRegistry<BaseTrapAction>> TRAP_ACTIONS_REGISTRY;
}
