package com.mapswitch.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapswitch.MapSwitchMod;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configFile;
    private Config config;

    public ConfigManager(Path configFile) {
        this.configFile = configFile;
    }

    public void loadOrCreate() {
        try {
            Files.createDirectories(configFile.getParent());
            if (Files.notExists(configFile)) {
                config = Config.defaultConfig();
                save();
                return;
            }

            try (Reader reader = Files.newBufferedReader(configFile)) {
                config = GSON.fromJson(reader, Config.class);
            }
            if (config == null) {
                config = Config.defaultConfig();
                save();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed loading config: " + configFile, ex);
        }
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(configFile)) {
            GSON.toJson(config, writer);
        } catch (IOException ex) {
            MapSwitchMod.LOGGER.error("[MapSwitch] Failed writing config {}", configFile, ex);
        }
    }

    public Config getConfig() {
        return config;
    }

    public static final class Config {
        public String default_map = "normal";
        public List<String> allowed_maps = new ArrayList<>(List.of("normal", "hardcore", "farmwelt", "bauwelt"));

        public static Config defaultConfig() {
            return new Config();
        }
    }
}
