package dev.sebastianb.chatformatter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ChatFormatter implements ModInitializer {

    private static String chatFormat = "<%s> %s";

    @Override
    public void onInitialize() {
        //Replace "modName" with your mod name"
        loadConfig(new File(FabricLoader.getInstance().getConfigDir().toFile(), "chatformatter.properties"));
    }

    // Seba notes: stole this config from the fabricord discord. Better ways to do this but I'm lazy
    public static void loadConfig(File file) {
        try {
            if (!file.exists() || !file.canRead()) {
                saveConfig(file);
            }
            FileInputStream fis = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fis);
            fis.close();
            //You can type Integer.parseInt, Double.parseDouble, etc.
            //Replace all mentions of sampleValue in the file with your variable name
            chatFormat = String.valueOf(properties.computeIfAbsent("chatFormat", a -> "<%s> %s"));
            saveConfig(file);
        } catch (Exception e) {
            //Make sure to set the default value in all places!
            chatFormat = "<%s> %s";
        }
    }
    public static void saveConfig(File file){
        try (FileOutputStream fos = new FileOutputStream(file, false)) {
            //Make sure to write values for every value in your config!
            fos.write(("chatFormat = " + chatFormat).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getChatFormat() {
        return chatFormat;
    }
}
