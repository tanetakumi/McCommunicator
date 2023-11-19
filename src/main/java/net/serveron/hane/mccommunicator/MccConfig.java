package net.serveron.hane.mccommunicator;

import com.moandjiezana.toml.Toml;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class MccConfig {
    private final File config;
    private String token;
    private String chatShareChannelId;
    private String playerListTitle;
    private String playerListChannelId;
    private String playerListMessageId;

    public MccConfig(File config) {
        this.config = config;
        try {
            load();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void load() throws FileNotFoundException, UnsupportedEncodingException {
        if(!config.exists()) {
            FabricLoader.getInstance().getModContainer(McCommunicator.MOD_ID).flatMap(m -> m.findPath(config.getName())).ifPresent(f -> {
                try {
                    Files.copy(f, config.toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        InputStream inputStream = new FileInputStream(config.getPath());
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        Toml toml = new Toml().read(inputStreamReader);

        token = toml.getString("discord.token");
        chatShareChannelId = toml.getString("discord.channelId");
        playerListTitle = toml.getString("discord.player-list.title");
        playerListChannelId = toml.getString("discord.player-list.channelId");
        playerListMessageId = toml.getString("discord.player-list.messageId");
    }

    private void write(String key, String preValue, String newValue, boolean isString) {
        try {
            String content = Files.readString(config.toPath());
            String updatedContent = isString ? content.replace(key+"=\""+preValue+"\"",key+"=\""+newValue+"\"") : content.replace(key+"="+preValue,key+"="+newValue);
            Files.writeString(config.toPath(), updatedContent, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getToken() {
        return token;
    }
    public String getChatShareChannelId() {
        return chatShareChannelId;
    }
    public String getPlayerListTitle() {
        return playerListTitle;
    }
    public String getPlayerListChannelId() {
        return playerListChannelId;
    }
    public String getPlayerListMessageId() {
        return playerListMessageId;
    }

    public void setPlayerListMessageId(String value) {
        write("messageId", playerListMessageId, value, true);
        playerListMessageId = value;
    }
}
