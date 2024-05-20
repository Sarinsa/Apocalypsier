package com.toast.apocalypse.common.network.work;

import com.toast.apocalypse.client.ClientUtil;
import com.toast.apocalypse.client.event.DifficultyRenderHandler;
import com.toast.apocalypse.client.screen.GrumpInventoryScreen;
import com.toast.apocalypse.client.screen.MobWikiScreen;
import com.toast.apocalypse.common.blockentity.DynamicTrapBlockEntity;
import com.toast.apocalypse.common.capability.ApocalypseCapabilities;
import com.toast.apocalypse.common.capability.difficulty.DifficultyProvider;
import com.toast.apocalypse.common.capability.mobwiki.MobWikiProvider;
import com.toast.apocalypse.common.core.register.ApocalypseTrapActions;
import com.toast.apocalypse.common.entity.living.Grump;
import com.toast.apocalypse.common.inventory.container.GrumpInventoryContainer;
import com.toast.apocalypse.common.network.message.*;
import com.toast.apocalypse.common.util.References;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;

/**
 * Referencing client only code here should cause no trouble
 * as long as this class isn't loaded by anything else
 * than the client itself (which should be the case).
 */
public class ClientWork {


    public static void handleDifficultyUpdate(S2CUpdatePlayerDifficulty message) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            player.getCapability(ApocalypseCapabilities.DIFFICULTY_CAPABILITY).orElse(DifficultyProvider.SUPPLIER.get()).setDifficulty(message.difficulty);
        }
    }


    public static void handleDifficultyRateUpdate(S2CUpdatePlayerDifficultyRate message) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            player.getCapability(ApocalypseCapabilities.DIFFICULTY_CAPABILITY).orElse(DifficultyProvider.SUPPLIER.get()).setDifficultyMult(message.multiplier);
        }
    }


    public static void handleMaxDifficultyUpdate(S2CUpdatePlayerMaxDifficulty message) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            long maxDifficulty = message.maxDifficulty;
            player.getCapability(ApocalypseCapabilities.DIFFICULTY_CAPABILITY).orElse(DifficultyProvider.SUPPLIER.get()).setMaxDifficulty(maxDifficulty);
            DifficultyRenderHandler.COLOR_CHANGE = maxDifficulty > -1 ? maxDifficulty : References.DEFAULT_COLOR_CHANGE;
        }
    }


    public static void handleEntityVelocityUpdate(S2CUpdateEntityVelocity message) {
        Level level = Minecraft.getInstance().level;

        if (level != null) {
            Entity entity = level.getEntity(message.entityId);

            if (entity != null) {
                entity.setDeltaMovement(message.xMotion, message.yMotion, message.zMotion);
            }
        }
    }


    public static void handleMoonPhaseUpdate(S2CUpdateMoonPhase message) {
        ClientUtil.OVERWORLD_MOON_PHASE = message.moonPhase;
    }


    public static void handleMobWikiIndexUpdate(S2CUpdateMobWikiIndexes message) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            int[] unlockedIndexes = message.indexes;
            player.getCapability(ApocalypseCapabilities.MOB_WIKI_CAPABILITY).orElse(MobWikiProvider.SUPPLIER.get()).setEntries(unlockedIndexes);
        }
    }


    public static void handleOpenMobWikiScreen(S2COpenMobWikiScreen message) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null && player.getUUID().equals(message.uuid))
            return;

        Minecraft.getInstance().setScreen(new MobWikiScreen());
    }


    public static void handleOpenGrumpInventory(S2COpenGrumpInventory message) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null || !(player.getUUID().equals(message.uuid)))
            return;

        ClientLevel level = Minecraft.getInstance().level;

        if (level == null)
            return;

        Entity entity = level.getEntity(message.entityID);

        if (!(entity instanceof Grump grump))
            return;

        Inventory playerInventory = Minecraft.getInstance().player.getInventory();

        GrumpInventoryContainer container = new GrumpInventoryContainer(message.containerId, playerInventory, grump.getInventory(), grump);
        Minecraft.getInstance().player.containerMenu = container;
        Minecraft.getInstance().setScreen(new GrumpInventoryScreen(container, playerInventory, grump));
    }


    public static void handleSimpleClientTaskRequest(S2CSimpleClientTask message) {
        if (message.action == S2CSimpleClientTask.SET_ACID_RAIN) {
            ClientUtil.ACID_RAIN_TICKER.setRainingAcid(true);
        }
        else if (message.action == S2CSimpleClientTask.REMOVE_ACID_RAIN) {
            ClientUtil.ACID_RAIN_TICKER.setRainingAcid(false);
        }
    }


    public static void handleDynTrapUpdate(S2CDynTrap message) {
        BlockPos pos = message.pos;
        ClientLevel level = Minecraft.getInstance().level;

        if (level.getExistingBlockEntity(pos) instanceof DynamicTrapBlockEntity trap) {
            if (message.id.isEmpty()) {
                trap.setCurrentTrap(null);
                return;
            }
            ResourceLocation id = ResourceLocation.tryParse(message.id);

            if (id == null)
                return;

            if (ApocalypseTrapActions.TRAP_ACTIONS_REGISTRY.get().containsKey(id)) {
                trap.setCurrentTrap(ApocalypseTrapActions.TRAP_ACTIONS_REGISTRY.get().getValue(id));
            }
        }
    }
}
