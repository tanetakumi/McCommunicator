package net.serveron.hane.mccommunicator.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.serveron.hane.mccommunicator.MccServer;
import net.serveron.hane.mccommunicator.util.Msg;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JDAManager extends ListenerAdapter {
    private JDA jda;

    public JDAManager() throws InterruptedException {
        initDiscordBot();
    }
    public void shutdown(){
        deInitDiscordBot();
    }

    @SuppressWarnings("ConstantConditions")
    public void sendMessage(String message){
        jda.getTextChannelById(MccServer.getConfig().getChatShareChannelId()).sendMessage(message).queue();
    }

    @SuppressWarnings("ConstantConditions")
    public void sendJoinMessage(String playerName){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor(playerName+" が参加しました", null,"https://cravatar.eu/avatar/"+playerName);
        eb.setColor(0x00ff00);
        jda.getTextChannelById(MccServer.getConfig().getChatShareChannelId()).sendMessageEmbeds(eb.build()).queue();
    }

    @SuppressWarnings("ConstantConditions")
    public void sendLeaveMessage(String playerName){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor(playerName+" が退出しました", null,"https://cravatar.eu/avatar/"+playerName);
        eb.setColor(0x999999);
        jda.getTextChannelById(MccServer.getConfig().getChatShareChannelId()).sendMessageEmbeds(eb.build()).queue();
    }

    @SuppressWarnings("ConstantConditions")
    public void initializeMessage(){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor("\uD83D\uDC25サーバーが起動しました。");
        eb.setColor(0x00ff00);
        jda.getTextChannelById(MccServer.getConfig().getChatShareChannelId()).sendMessageEmbeds(eb.build()).queue();
    }

    public void updatePlayerCountMessage(String channelId, String messageId, Map<String, String> serverPlayerMap, int playerCount){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(MccServer.getConfig().getPlayerListTitle());
        eb.setDescription("プレイヤー : "+playerCount+"人");
        serverPlayerMap.forEach((server, players) -> eb.addField(server, players, false));

        eb.setFooter(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
        eb.setColor(0x08ff4d);

        TextChannel textChannel = jda.getTextChannelById(channelId);
        if(textChannel!=null){
            try {
                textChannel.editMessageEmbedsById(messageId, eb.build()).queue();
            } catch (Exception e) {
                textChannel.sendMessageEmbeds(eb.build()).queue((message) -> {
                    MccServer.getConfig().setPlayerListMessageId(message.getId());
                });
            }
        } else {
            Msg.sendToConsole("TextChannelが見つかりませんでした。", Msg.MessageType.ERROR);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        String id = e.getChannel().getId();
        if(id.equals(MccServer.getConfig().getChatShareChannelId())){
            if(!e.getAuthor().isBot()){
                String username = (e.getMember() != null && e.getMember().getNickname()!=null) ? e.getMember().getNickname() : e.getAuthor().getName();
                String formattedMessage = formatMessage(e.getMessage());

                MccServer.minecraftServer.getPlayerManager().broadcast(
                        Text.literal("[Discord]").formatted(Formatting.GREEN)
                                .append(Text.literal("<"+username+"> "+formattedMessage).formatted(Formatting.WHITE)),
                        false
                );
            }
        }
    }

    private String formatMessage(Message message){
        String text = message.getContentDisplay();
        text = text.replace("(https?://\\S+)(\\s|$)", "(URL)");

        if(message.getAttachments().size() > 0) {
            text = "(添付ファイル) " + text;
        }
        if(message.getReferencedMessage() != null) {
            text = ">> " + text;
        }
        if(text.length() > 128) {
            text = text.substring(0, 127) + "(以下略)";
        }
        return text;
    }


    private void initDiscordBot() throws InterruptedException {
        String token = MccServer.getConfig().getToken();
        if(token.isEmpty()){
            Msg.sendToConsole("Discord bot is disabled due to the empty token", Msg.MessageType.WARNING);
        } else {
            Msg.sendToConsole("Initialize JDA");
            if(jda==null){
                jda = JDABuilder.createLight(token)
                        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                        .addEventListeners(this)
                        .build().awaitReady();

                initializeMessage();
            } else {
                Msg.sendToConsole("JDAは起動しています。", Msg.MessageType.WARNING);
            }
        }
    }

    private void deInitDiscordBot(){
        if (jda != null) {
            jda.getEventManager().getRegisteredListeners().forEach(listener -> jda.getEventManager().unregister(listener));
            jda.shutdown();
        }
    }
}
