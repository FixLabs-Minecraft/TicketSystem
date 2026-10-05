package com.github.henriquemb.ticketsystem.gui;

import com.github.henriquemb.ticketsystem.TicketSystem;
import com.github.henriquemb.ticketsystem.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Сборка предметов меню из раздела gui языкового файла.
 */
public final class Icons {
    private static final int WRAP_WIDTH = 40;

    private final Map<String, String> placeholders = new LinkedHashMap<>();
    private final Map<String, String> multiline = new LinkedHashMap<>();

    /**
     * Обычная подстановка: значение вставляется как есть (может содержать &-коды).
     */
    public Icons with(String key, Object value) {
        placeholders.put("<" + key + ">", value == null ? "" : String.valueOf(value));
        return this;
    }

    /**
     * Текст игрока: экранируется и при необходимости переносится на несколько строк описания.
     */
    public Icons text(String key, String value) {
        multiline.put("<" + key + ">", value == null ? "" : value);
        return this;
    }

    public ItemStack build(Material material, String path) {
        FileConfiguration messages = TicketSystem.getMessages();

        String name = apply(messages.getString(path + ".name", ""));
        List<Component> lore = new ArrayList<>();
        for (String line : messages.getStringList(path + ".lore")) {
            for (String expanded : expand(apply(line))) lore.add(component(expanded));
        }

        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            meta.displayName(component(name));
            meta.lore(lore);
        });
        return item;
    }

    private String apply(String line) {
        for (Map.Entry<String, String> e : placeholders.entrySet()) line = line.replace(e.getKey(), e.getValue());
        return line;
    }

    private List<String> expand(String line) {
        for (Map.Entry<String, String> e : multiline.entrySet()) {
            if (!line.contains(e.getKey())) continue;

            List<String> result = new ArrayList<>();
            for (String chunk : wrap(e.getValue())) {
                String replaced = line.replace(e.getKey(), Text.escape(chunk));
                result.addAll(expand(replaced));
            }
            return result;
        }
        return List.of(line);
    }

    private static List<String> wrap(String text) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String word : text.trim().split("\\s+")) {
            if (!current.isEmpty() && current.length() + word.length() + 1 > WRAP_WIDTH) {
                lines.add(current.toString());
                current.setLength(0);
            }
            if (!current.isEmpty()) current.append(' ');
            current.append(word);
        }
        if (!current.isEmpty() || lines.isEmpty()) lines.add(current.toString());

        return lines;
    }

    private static Component component(String line) {
        return Text.parse(line).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "—";
        return new SimpleDateFormat(TicketSystem.getMessages().getString("gui.date-format", "dd.MM.yyyy HH:mm")).format(timestamp);
    }
}
