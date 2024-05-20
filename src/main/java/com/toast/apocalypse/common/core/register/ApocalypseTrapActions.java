package com.toast.apocalypse.common.core.register;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.trap_actions.BaseTrapAction;
import com.toast.apocalypse.common.trap_actions.GhostTrap;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

public class ApocalypseTrapActions {

    public static Supplier<IForgeRegistry<BaseTrapAction>> TRAP_ACTIONS_REGISTRY;
    public static final DeferredRegister<BaseTrapAction> TRAP_ACTIONS = DeferredRegister.create(ResourceKey.createRegistryKey(Apocalypse.resourceLoc("trap_actions")), Apocalypse.MODID);



    public static final RegistryObject<GhostTrap> GHOST_TRAP = TRAP_ACTIONS.register("ghost_trap", GhostTrap::new);


    public static void onRegistryCreate(NewRegistryEvent event) {
        RegistryBuilder<BaseTrapAction> builder = new RegistryBuilder<>();
        builder.setName(Apocalypse.resourceLoc("trap_actions"));
        TRAP_ACTIONS_REGISTRY = event.create(builder);
    }
}
