package com.github.henriquemb.ticketsystem;

import com.github.henriquemb.ticketsystem.util.Text;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class Model {
    private final FileConfiguration messages = TicketSystem.getMessages();

    private final String ticketPrefix = messages.getString("prefix.ticket");
    private final String reportPrefix = messages.getString("prefix.report");
    private final String suggestionPrefix = messages.getString("prefix.suggestion");

    private final Map<UUID, Timestamp> supportCommandDelay = new HashMap<>();

    public void sendMessage(CommandSender p, String message) {
        try {
            p.sendMessage(Text.parse(message));
        }
        catch (Exception e) {
            TicketSystem.getMain().getLogger().warning("Сообщение не найдено в языковом файле, проверьте его.");
            p.sendMessage(Component.text("Внутренняя ошибка.", NamedTextColor.RED));
        }
    }

    public void sendMessage(CommandSender p, String message, String prefix) {
        try {
            sendMessage(p, messages.getString(String.format("prefix.%s", prefix)).concat(message));
        }
        catch (Exception e) {
            TicketSystem.getMain().getLogger().warning("Сообщение не найдено в языковом файле, проверьте его.");
            p.sendMessage(Component.text("Внутренняя ошибка.", NamedTextColor.RED));
        }
    }

    public void broadcastMessage(String message) {
        Component component = Text.parse(message);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(component));
    }
}
