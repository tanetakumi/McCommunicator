package net.serveron.hane.mccommunicator.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.serveron.hane.mccommunicator.McCommunicator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Msg {
    private static final HttpClient client;
    static {
        client = HttpClient.newHttpClient();

    }
    public enum MessageType{
        INFO,
        WARNING,
        ERROR,
        GOLD,
        AQUA
    }
    public static void sendToConsole(String msg){sendToConsole(msg, MessageType.INFO);}
    public static void sendToConsole(String msg, MessageType type){
        switch (type) {
            case ERROR -> McCommunicator.getLogger().info("\u001B[31m" + msg + "\u001B[0m");
            case WARNING -> McCommunicator.getLogger().info("\u001B[33m" + msg + "\u001B[0m");
            default -> McCommunicator.getLogger().info(msg);
        }
    }

    public static void sendToPlayer(ServerPlayerEntity player, String msg) { sendToPlayer(player, msg, MessageType.INFO); }
    public static void sendToPlayer(ServerPlayerEntity player, String msg, MessageType type){
        switch (type) {
            case ERROR -> player.sendMessage(Text.literal(msg).formatted(Formatting.RED), false);
            case WARNING -> player.sendMessage(Text.literal(msg).formatted(Formatting.YELLOW), false);
            case GOLD -> player.sendMessage(Text.literal(msg).formatted(Formatting.GOLD), false);
            case AQUA -> player.sendMessage(Text.literal(msg).formatted(Formatting.AQUA), false);
            default -> player.sendMessage(Text.literal(msg), false);
        }
    }

    public static void sendToDiscord(String msg, String webhook) {
        if(webhook!=null && !webhook.isEmpty()){
            HttpRequest request = HttpRequest
                    .newBuilder(URI.create(webhook))
                    .setHeader("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"HaneAsist\",\"content\":\""+ msg +"\"}"))
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } else {
            sendToConsole("DiscordWebhookが読み込めませんでした。", MessageType.ERROR);
        }
    }
}
