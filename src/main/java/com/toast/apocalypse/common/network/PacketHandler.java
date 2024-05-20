package com.toast.apocalypse.common.network;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.network.message.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler {

    private static final String PROTOCOL_NAME = "APOCALYPSE";
    /** The network channel our mod will be
     *  using when sending messages. */
    public static final SimpleChannel CHANNEL = createChannel();

    private int messageIndex;

    private static SimpleChannel createChannel() {
        return NetworkRegistry.ChannelBuilder
                .named(Apocalypse.resourceLoc("channel"))
                .serverAcceptedVersions(PROTOCOL_NAME::equals)
                .clientAcceptedVersions(PROTOCOL_NAME::equals)
                .networkProtocolVersion(() -> PROTOCOL_NAME)
                .simpleChannel();
    }

    public final void registerMessages() {
        // Server -> Client
        registerMessage(S2CUpdatePlayerDifficulty.class, S2CUpdatePlayerDifficulty::encode, S2CUpdatePlayerDifficulty::decode, S2CUpdatePlayerDifficulty::handle);
        registerMessage(S2CUpdatePlayerDifficultyRate.class, S2CUpdatePlayerDifficultyRate::encode, S2CUpdatePlayerDifficultyRate::decode, S2CUpdatePlayerDifficultyRate::handle);
        registerMessage(S2CUpdatePlayerMaxDifficulty.class, S2CUpdatePlayerMaxDifficulty::encode, S2CUpdatePlayerMaxDifficulty::decode, S2CUpdatePlayerMaxDifficulty::handle);
        registerMessage(S2CUpdateEntityVelocity.class, S2CUpdateEntityVelocity::encode, S2CUpdateEntityVelocity::decode, S2CUpdateEntityVelocity::handle);
        registerMessage(S2CUpdateMoonPhase.class, S2CUpdateMoonPhase::encode, S2CUpdateMoonPhase::decode, S2CUpdateMoonPhase::handle);
        registerMessage(S2CUpdateMobWikiIndexes.class, S2CUpdateMobWikiIndexes::encode, S2CUpdateMobWikiIndexes::decode, S2CUpdateMobWikiIndexes::handle);
        registerMessage(S2COpenMobWikiScreen.class, S2COpenMobWikiScreen::encode, S2COpenMobWikiScreen::decode, S2COpenMobWikiScreen::handle);
        registerMessage(S2COpenGrumpInventory.class, S2COpenGrumpInventory::encode, S2COpenGrumpInventory::decode, S2COpenGrumpInventory::handle);
        registerMessage(S2CSimpleClientTask.class, S2CSimpleClientTask::encode, S2CSimpleClientTask::decode, S2CSimpleClientTask::handle);
        registerMessage(S2CDynTrap.class, S2CDynTrap::encode, S2CDynTrap::decode, S2CDynTrap::handle);

        // Client -> Server
        registerMessage(C2SOpenGrumpInventory.class, C2SOpenGrumpInventory::encode, C2SOpenGrumpInventory::decode, C2SOpenGrumpInventory::handle);
        registerMessage(C2SUpdateGrumpDescent.class, C2SUpdateGrumpDescent::encode, C2SUpdateGrumpDescent::decode, C2SUpdateGrumpDescent::handle);
    }

    public <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        CHANNEL.registerMessage(this.messageIndex++, messageType, encoder, decoder, messageConsumer, Optional.empty());
    }

    /**
     * Sends the specified message to the client.
     *
     * @param message The message to send to the client.
     * @param player The player client that should receive this message.
     * @param <MSG> Packet type.
     */
    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        CHANNEL.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}