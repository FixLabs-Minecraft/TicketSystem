package com.github.henriquemb.ticketsystem;

import com.github.henriquemb.ticketsystem.commands.CommandRegister;
import com.github.henriquemb.ticketsystem.database.Database;
import com.github.henriquemb.ticketsystem.events.ListenerRegister;
import com.github.henriquemb.ticketsystem.gui.Menu;
import com.github.henriquemb.ticketsystem.util.CustomConfig;
import com.github.henriquemb.ticketsystem.util.Settings;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class TicketSystem extends JavaPlugin {
    private static final String DEFAULT_LANGUAGE = "russian";

    @Getter @Setter
    private static TicketSystem main;
    @Getter @Setter
    private static Model model;
    @Getter @Setter
    private static FileConfiguration messages;
    @Getter @Setter
    private static Settings settings;

    @Override
    public void onEnable() {
        setMain(this);

        saveDefaultConfig();
        // Новые параметры config.yml подхватываются из встроенного файла
        getConfig().options().copyDefaults(true);
        saveConfig();

        load();
    }

    /**
     * Перечитывает config.yml и языковой файл, переподключается к базе данных,
     * заново регистрирует команды и слушатели.
     */
    public void reload() {
        closeMenus();
        reloadConfig();
        HandlerList.unregisterAll(this);
        load();
    }

    private void load() {
        setSettings(new Settings(getConfig(), getLogger()));

        CustomConfig.createCustomConfig("language/" + DEFAULT_LANGUAGE);
        CustomConfig.createCustomConfig("language/english");

        String language = getConfig().getString("language", DEFAULT_LANGUAGE);
        if (!new File(getDataFolder(), "language/" + language + ".yml").exists()) {
            getLogger().warning("Языковой файл language/" + language + ".yml не найден, используется " + DEFAULT_LANGUAGE + ".yml");
            language = DEFAULT_LANGUAGE;
        }
        setMessages(CustomConfig.createCustomConfig("language/" + language, "language/" + DEFAULT_LANGUAGE));

        Database.connect(getSettings().getDatabaseSection(), getDataFolder(), getLogger());

        setModel(new Model());

        new CommandRegister(this);
        new ListenerRegister(this);
    }

    private void closeMenus() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof Menu) player.closeInventory();
        });
    }

    @Override
    public void onDisable() {
        closeMenus();
        Database.close();
    }
}
