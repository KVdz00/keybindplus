package com.github.kvdz00.keybindplus.config;

import com.github.kvdz00.keybindplus.KeybindPlus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class KeybindPlusConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private String defaultProfile = "";
    private boolean showLoadNotification = true;
    private boolean autoLoadDefaultOnStartup = true;

    private static KeybindPlusConfig instance;
    private static Path configPath;

    private KeybindPlusConfig() {}

    public static KeybindPlusConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static Path getConfigDir() {
        return Minecraft.getInstance().gameDirectory.toPath()
            .resolve("config").resolve("keybindplus");
    }

    public static Path getProfilesDir() {
        return getConfigDir().resolve("profiles");
    }

    public static Path getExportsDir() {
        return getConfigDir().resolve("exports");
    }

    public static Path getImportsDir() {
        return getConfigDir().resolve("imports");
    }

    public static Path getBackupsDir() {
        return getConfigDir().resolve("backups");
    }

    public static void load() {
        configPath = getConfigDir().resolve("config.json");
        try {
            Files.createDirectories(getConfigDir());
            Files.createDirectories(getProfilesDir());
            Files.createDirectories(getExportsDir());
            Files.createDirectories(getImportsDir());
            Files.createDirectories(getBackupsDir());

            if (Files.exists(configPath)) {
                String json = Files.readString(configPath);
                instance = GSON.fromJson(json, KeybindPlusConfig.class);
            } else {
                instance = new KeybindPlusConfig();
                save();
            }
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to load config: {}", e.getMessage());
            instance = new KeybindPlusConfig();
        }
    }

    public static void save() {
        if (instance == null) instance = new KeybindPlusConfig();
        try {
            Files.createDirectories(getConfigDir());
            Files.writeString(configPath, GSON.toJson(instance));
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }

    public String getDefaultProfile() { return defaultProfile; }
    public void setDefaultProfile(String name) { this.defaultProfile = name; save(); }
    public boolean isShowLoadNotification() { return showLoadNotification; }
    public boolean isAutoLoadDefaultOnStartup() { return autoLoadDefaultOnStartup; }
}
