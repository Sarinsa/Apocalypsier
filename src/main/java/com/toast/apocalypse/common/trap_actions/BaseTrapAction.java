package com.toast.apocalypse.common.trap_actions;

import com.toast.apocalypse.common.core.register.ApocalypseTrapActions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public abstract class BaseTrapAction {

    private String descriptionId;

    public BaseTrapAction() {
    }

    public abstract void execute(Level level, BlockPos pos, boolean facingUp);

    @Nonnull
    public abstract ResourceLocation iconLocation();

    public abstract MutableComponent getDescription();

    public final String getNameTranslationKey(BaseTrapAction trapAction) {
        if (descriptionId == null) {
            ResourceLocation id = ApocalypseTrapActions.TRAP_ACTIONS_REGISTRY.get().getKey(trapAction);
            descriptionId = "apocalypse.trap_type." + id.getNamespace() + "." + id.getPath() + ".name";
        }
        return descriptionId;
    }
}
