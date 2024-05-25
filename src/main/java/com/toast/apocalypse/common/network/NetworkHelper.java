package com.toast.apocalypse.common.network;

import com.toast.apocalypse.api.register.ModRegistries;
import com.toast.apocalypse.common.blockentity.DynamicTrapBlockEntity;
import com.toast.apocalypse.common.core.register.ApocalypseTrapActions;
import com.toast.apocalypse.common.entity.living.Grump;
import com.toast.apocalypse.common.network.message.*;
import com.toast.apocalypse.common.util.CapabilityHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.UUID;

/** Helper class for easily sending messages */
public class NetworkHelper {

    /**
     * Sends a message from the server to client
     * to inform of a change in world difficulty rate.
     *
     * @param multiplier The new difficulty multiplier.
     */
    public static void sendUpdatePlayerDifficultyMult(@Nonnull ServerPlayer player, double multiplier) {
        PacketHandler.sendToClient(new S2CUpdatePlayerDifficultyRate(multiplier), player);
    }

    public static void sendUpdatePlayerDifficultyMult(@Nonnull ServerPlayer player) {
        PacketHandler.sendToClient(new S2CUpdatePlayerDifficultyRate(CapabilityHelper.getPlayerDifficultyMult(player)), player);
    }

    /**
     * Sends a message from the server to client
     * to inform that the player's difficulty changed.
     *
     * @param difficulty The player's new difficulty.
     */
    public static void sendUpdatePlayerDifficulty(@Nonnull ServerPlayer player, long difficulty) {
        PacketHandler.sendToClient(new S2CUpdatePlayerDifficulty(difficulty), player);
    }

    public static void sendUpdatePlayerDifficulty(@Nonnull ServerPlayer player) {
        PacketHandler.sendToClient(new S2CUpdatePlayerDifficulty(CapabilityHelper.getPlayerDifficulty(player)), player);
    }

    /**
     * Sends a message from the server to client
     * to inform that the player's max difficulty changed.
     *
     * @param maxDifficulty The player's new max difficulty.
     */
    public static void sendUpdatePlayerMaxDifficulty(@Nonnull ServerPlayer player, long maxDifficulty) {
        PacketHandler.sendToClient(new S2CUpdatePlayerMaxDifficulty(maxDifficulty), player);
    }

    public static void sendUpdatePlayerMaxDifficulty(@Nonnull ServerPlayer player) {
        PacketHandler.sendToClient(new S2CUpdatePlayerMaxDifficulty(CapabilityHelper.getMaxPlayerDifficulty(player)), player);
    }

    /**
     * Sends a message from the server to client
     * to inform that the specified entity's
     * velocity has changed.
     *
     * @param entity The entity to update velocity for.
     * @param deltaMovement The new velocity vector.
     */
    public static void sendEntityVelocityUpdate(@Nonnull ServerPlayer player, Entity entity, Vec3 deltaMovement) {
        PacketHandler.sendToClient(new S2CUpdateEntityVelocity(entity, deltaMovement), player);
    }

    /**
     * Sends a message from the server to client
     * to update the overworld moon phase
     * value in {@link com.toast.apocalypse.client.ClientUtil}
     */
    public static void sendMoonPhaseUpdate(@Nonnull ServerPlayer player, ServerLevel overworld) {
        PacketHandler.sendToClient(new S2CUpdateMoonPhase(overworld.dimensionType().moonPhase(overworld.getDayTime())), player);
    }

    /**
     * Sends a message from the server to client
     * to update unlocked mob wiki indexes.
     */
    public static void sendMobWikiIndexUpdate(@Nonnull ServerPlayer player, int[] unlockedIndexes) {
        PacketHandler.sendToClient(new S2CUpdateMobWikiIndexes(unlockedIndexes), player);
    }

    /**
     * Used when a player joins the world.
     */
    public static void sendMobWikiIndexUpdate(@Nonnull ServerPlayer player) {
        PacketHandler.sendToClient(new S2CUpdateMobWikiIndexes(CapabilityHelper.getMobWikiIndexes(player)), player);
    }

    /**
     * Sends a message from the server to client
     * to open the mob wiki screen.
     */
    public static void openMobWikiScreen(@Nonnull ServerPlayer player) {
        PacketHandler.sendToClient(new S2COpenMobWikiScreen(player.getUUID()), player);
    }

    public static void sendDynTrapUpdate(@Nonnull ServerLevel level, DynamicTrapBlockEntity trap) {
        if (trap == null)
            return;

        String id = trap.getCurrentTrap() == null
                ? ""
                : ModRegistries.TRAP_ACTIONS_REGISTRY.get().getKey(trap.getCurrentTrap()).toString();

        for (ServerPlayer player : level.players()) {
            PacketHandler.sendToClient(new S2CDynTrap(trap.getBlockPos(), id), player);
        }
    }

    /**
     * Sends a message from the server to client
     * to request one of the listed tasks depending on
     * the value of "action":<br>
     * <br>
     *
     * 0 - Replaces weather render handlers with acid rain render handlers.<br>
     * 1 - Removes acid rain weather render handlers from WorldRenderer if present.
     */
    public static void sendSimpleClientTaskRequest(@Nonnull ServerPlayer player, byte action) {
        PacketHandler.sendToClient(new S2CSimpleClientTask(action), player);
    }

    public static void openGrumpInventory(@Nonnull ServerPlayer player, int containerId, @Nonnull Grump grump) {
        PacketHandler.sendToClient(new S2COpenGrumpInventory(player.getUUID(), containerId, grump.getId()), player);
    }

    public static void requestOpenGrumpInventory(@Nonnull UUID playerUUID) {
        PacketHandler.CHANNEL.sendToServer(new C2SOpenGrumpInventory(playerUUID));
    }

    public static void requestGrumpDescentUpdate(@Nonnull UUID playerUUID, boolean keyReleased) {
        PacketHandler.CHANNEL.sendToServer(new C2SUpdateGrumpDescent(playerUUID, keyReleased));
    }
}
