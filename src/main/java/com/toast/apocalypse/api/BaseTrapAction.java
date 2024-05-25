package com.toast.apocalypse.api;

import com.toast.apocalypse.api.register.ModRegistries;
import com.toast.apocalypse.common.core.register.ApocalypseTrapActions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.registries.NewRegistryEvent;

import javax.annotation.Nonnull;

/**
 * Represents a trap type that can be crafted and used by the Dynamic Trap block.
 * Create an implementation of this and register it to the {@link ModRegistries#TRAP_ACTIONS_REGISTRY}
 * with a deferred register.
 */
public abstract class BaseTrapAction {

    private String descriptionId;

    public BaseTrapAction() {

    }

    /**
     * The actual logic this trap runs when activated.<p></p>
     * @param pos The block position of the Dynamic Trap.
     * @param facingUp True if the Dynamic Trap is facing upwards. False if not.
     */
    public abstract void execute(Level level, BlockPos pos, boolean facingUp);

    /**
     * @return A ResourceLocation pointing to this trap type's icon.
     * icon must be 16x16 currently.
     */
    @Nonnull
    public abstract ResourceLocation iconLocation();

    /**
     * @return A translation key for a description that describes what this trap type does when activated.
     * Used when hovering over a ready trap-type in the Dynamic Trap GUI.
     */
    public abstract String getDescriptionKey();


    @SuppressWarnings("ConstantConditions")
    public final String getNameTranslationKey(BaseTrapAction trapAction) {
        if (descriptionId == null) {
            ResourceLocation id = ModRegistries.TRAP_ACTIONS_REGISTRY.get().getKey(trapAction);
            descriptionId = "apocalypse.trap_type." + id.getNamespace() + "." + id.getPath() + ".name";
        }
        return descriptionId;
    }
}
