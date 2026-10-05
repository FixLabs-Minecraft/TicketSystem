package com.github.henriquemb.ticketsystem.util;

import com.github.henriquemb.ticketsystem.TicketSystem;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class CustomConfig {
    public static FileConfiguration createCustomConfig(String name) {
        return createCustomConfig(name, null);
    }

    /**
     * Загружает YAML-файл из папки плагина (создаёт его из ресурсов, если его нет).
     * Ключи, которых нет в файле (например, после обновления плагина), берутся из встроенного
     * ресурса с тем же именем, а затем из {@code fallbackResource}.
     */
    public static FileConfiguration createCustomConfig(String name, String fallbackResource) {
        File f = new File(TicketSystem.getMain().getDataFolder(), name + ".yml");

        if (!f.exists()) {
            f.getParentFile().mkdirs();
            TicketSystem.getMain().saveResource(name + ".yml", false);
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(f);
        }
        catch (IOException | InvalidConfigurationException e) {
            TicketSystem.getMain().getLogger().log(Level.SEVERE, "Не удалось загрузить " + f.getName(), e);
        }

        YamlConfiguration defaults = loadResource(name + ".yml");
        if (fallbackResource != null) {
            YamlConfiguration fallback = loadResource(fallbackResource + ".yml");
            if (defaults == null) defaults = fallback;
            else if (fallback != null) defaults.setDefaults(fallback);
        }
        if (defaults != null) config.setDefaults(defaults);

        return config;
    }

    private static YamlConfiguration loadResource(String path) {
        InputStream stream = TicketSystem.getMain().getResource(path);
        if (stream == null) return null;

        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        }
        catch (IOException e) {
            return null;
        }
    }
}
