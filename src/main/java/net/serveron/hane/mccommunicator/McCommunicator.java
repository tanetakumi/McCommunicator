package net.serveron.hane.mccommunicator;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class McCommunicator implements ModInitializer {
    public static final String MOD_ID = "mcc";
    private static final String SERVER_CONFIG = "mcc.toml";
    private static Logger logger;

    @Override
    public void onInitialize() {
        logger = LoggerFactory.getLogger(MOD_ID);
        logger.info("-- ModInitializer --");
    }
    public static Logger getLogger() { return logger; }
    public static String getServerConfig() {
        return SERVER_CONFIG;
    }
}
