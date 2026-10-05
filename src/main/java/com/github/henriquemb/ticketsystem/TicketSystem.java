package com.github.henriquemb.ticketsystem;

import com.github.henriquemb.ticketsystem.commands.CommandRegister;
import com.github.henriquemb.ticketsystem.database.factory.CreateDatabase;
import com.github.henriquemb.ticketsystem.events.ListenerRegister;
import com.github.henriquemb.ticketsystem.util.CustomConfig;
import lombok.Getter;
import lombok.Setter;
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

    @Override
    public void onEnable() {
        setMain(this);

        saveDefaultConfig();

        new CreateDatabase();

        load();
    }

    /**
     * Перечитывает config.yml и языковой файл, заново регистрирует команды и слушатели.
     */
    public void reload() {
        reloadConfig();
        HandlerList.unregisterAll(this);
        load();
    }

    private void load() {
        CustomConfig.createCustomConfig("language/" + DEFAULT_LANGUAGE);
        CustomConfig.createCustomConfig("language/english");

        String language = getConfig().getString("language", DEFAULT_LANGUAGE);
        if (!new File(getDataFolder(), "language/" + language + ".yml").exists()) {
            getLogger().warning("Языковой файл language/" + language + ".yml не найден, используется " + DEFAULT_LANGUAGE + ".yml");
            language = DEFAULT_LANGUAGE;
        }
        setMessages(CustomConfig.createCustomConfig("language/" + language));

        setModel(new Model());

        new CommandRegister(this);
        new ListenerRegister(this);
    }

    @Override
    public void onDisable() {
        // Логика выключения плагина
    }
}
