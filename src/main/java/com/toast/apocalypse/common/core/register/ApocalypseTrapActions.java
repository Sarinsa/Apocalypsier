package com.toast.apocalypse.common.core.register;

import com.toast.apocalypse.api.BaseTrapAction;
import com.toast.apocalypse.api.register.ModRegistries;
import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.trap_actions.EquipmentBreakTrap;
import com.toast.apocalypse.common.trap_actions.GhostTrap;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ApocalypseTrapActions {

    public static final DeferredRegister<BaseTrapAction> TRAP_ACTIONS = DeferredRegister.create(ResourceKey.createRegistryKey(Apocalypse.resourceLoc("trap_actions")), Apocalypse.MODID);


    public static final RegistryObject<GhostTrap> GHOST_FREEZE = TRAP_ACTIONS.register("ghost_freeze", GhostTrap::new);
    public static final RegistryObject<EquipmentBreakTrap> EQUIPMENT_BREAK = TRAP_ACTIONS.register("equipment_break", EquipmentBreakTrap::new);



    public static void onRegistryCreate(NewRegistryEvent event) {
        RegistryBuilder<BaseTrapAction> builder = new RegistryBuilder<>();
        builder.setName(Apocalypse.resourceLoc("trap_actions"));
        ModRegistries.TRAP_ACTIONS_REGISTRY = event.create(builder);
    }
}
