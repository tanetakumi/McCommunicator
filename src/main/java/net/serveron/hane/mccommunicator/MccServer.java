package net.serveron.hane.mccommunicator;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.tick.SimpleTickScheduler;
import net.serveron.hane.mccommunicator.discord.JDAManager;
import org.apache.commons.collections4.map.HashedMap;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MccServer implements DedicatedServerModInitializer {
    private static MccConfig serverConfig;
    public static MccConfig getConfig() { return serverConfig; }
    public static MinecraftServer minecraftServer;
    public JDAManager jdaManager;
    private ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
    @Override
    public void onInitializeServer() {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().resolve(McCommunicator.getServerConfig()).toUri());
        serverConfig = new MccConfig(configFile);

        if(!serverConfig.getToken().isEmpty()){

            ServerLifecycleEvents.SERVER_STARTED.register(server -> minecraftServer = server);
            ServerPlayConnectionEvents.JOIN.register(this::onJoin);
            ServerPlayConnectionEvents.DISCONNECT.register(this::onLeave);
            ServerMessageEvents.CHAT_MESSAGE.register(this::onChat);

            try {
                jdaManager = new JDAManager();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            threadPool.scheduleWithFixedDelay(() -> {
                String[] players =  minecraftServer.getPlayerManager().getPlayerNames();
                String playerMap = String.join(", ", players);

                jdaManager.updatePlayerCountMessage(
                        serverConfig.getPlayerListChannelId(),
                        serverConfig.getPlayerListMessageId(),
                        Map.of("server", playerMap),
                        players.length
                );
            }, 30, 300, TimeUnit.SECONDS);
        }

        ServerLifecycleEvents.SERVER_STOPPING.register(this::onStoppingServer);
    }

    private void onJoin(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer server) {
        jdaManager.sendJoinMessage(serverPlayNetworkHandler.player.getName().getString());
    }
    private void onLeave(ServerPlayNetworkHandler serverPlayNetworkHandler, MinecraftServer server) {
        jdaManager.sendLeaveMessage(serverPlayNetworkHandler.player.getName().getString());
    }
    private void onChat(SignedMessage signedMessage, ServerPlayerEntity serverPlayerEntity, MessageType.Parameters parameters) {
        jdaManager.sendMessage("<"+serverPlayerEntity.getName().getString()+"> "+signedMessage.getContent().getString());
    }

    private void onStoppingServer(MinecraftServer minecraftServer) {
        McCommunicator.getLogger().warn("---- stopping ----");
        threadPool.shutdown();
        if(jdaManager!=null){
            jdaManager.shutdown();
        }
    }
}
