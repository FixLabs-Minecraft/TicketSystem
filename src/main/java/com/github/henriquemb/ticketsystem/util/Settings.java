package com.github.henriquemb.ticketsystem.util;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * Значения из config.yml, прочитанные один раз при загрузке/перезагрузке плагина.
 */
public final class Settings {
    /** Ограничения для одного типа обращений (ticket, report, suggestion). */
    public record Limits(int minWords, int cooldownSeconds, int maxOpen) {
    }

    private final FileConfiguration config;
    private final Logger logger;

    private final Limits ticket;
    private final Limits report;
    private final Limits suggestion;
    private final int supportCooldownMinutes;
    private final int pageSize;
    private final Sound sound;

    public Settings(FileConfiguration config, Logger logger) {
        this.config = config;
        this.logger = logger;

        ticket = limits("ticket", 4, 60, 3);
        report = limits("report", 0, 60, 5);
        suggestion = limits("suggestion", 4, 120, 3);
        supportCooldownMinutes = Math.max(0, config.getInt("support-cooldown", 10));
        pageSize = Math.clamp(config.getInt("pagination.page-size", 10), 1, 50);
        sound = parseSound();
    }

    private Limits limits(String path, int minWords, int cooldown, int maxOpen) {
        return new Limits(
                Math.max(0, config.getInt(path + ".min-words", minWords)),
                Math.max(0, config.getInt(path + ".cooldown", cooldown)),
                Math.max(0, config.getInt(path + ".max-open", maxOpen))
        );
    }

    private Sound parseSound() {
        String name = config.getString("notifications.sound", "entity.experience_orb.pickup");
        if (name == null || name.isBlank()) return null;

        try {
            return Sound.sound(
                    Key.key(name.toLowerCase()),
                    Sound.Source.MASTER,
                    (float) config.getDouble("notifications.volume", 1.0),
                    (float) config.getDouble("notifications.pitch", 1.0)
            );
        }
        catch (InvalidKeyException e) {
            logger.warning("Некорректный звук notifications.sound: " + name);
            return null;
        }
    }

    public Limits limits(String type) {
        return switch (type) {
            case "ticket" -> ticket;
            case "report" -> report;
            case "suggestion" -> suggestion;
            default -> throw new IllegalArgumentException(type);
        };
    }

    public int getSupportCooldownMinutes() {
        return supportCooldownMinutes;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void playNotification(Player player) {
        if (sound != null) player.playSound(sound, Sound.Emitter.self());
    }

    public boolean isGuiEnabled() {
        return config.getBoolean("gui.enabled", true);
    }

    /**
     * Материал иконки из раздела gui.icons.
     */
    public Material icon(String name, Material fallback) {
        String value = config.getString("gui.icons." + name);
        if (value == null) return fallback;

        Material material = Material.matchMaterial(value);
        if (material == null || !material.isItem()) {
            logger.warning("Некорректный материал gui.icons." + name + ": " + value);
            return fallback;
        }
        return material;
    }

    public ConfigurationSection getDatabaseSection() {
        ConfigurationSection section = config.getConfigurationSection("database");
        return section != null ? section : config.createSection("database");
    }
}
