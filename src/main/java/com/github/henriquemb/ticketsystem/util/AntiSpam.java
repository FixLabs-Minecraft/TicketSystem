package com.github.henriquemb.ticketsystem.util;

import com.github.henriquemb.ticketsystem.TicketSystem;
import com.github.henriquemb.ticketsystem.database.controller.ReportController;
import com.github.henriquemb.ticketsystem.database.controller.SuggestionController;
import com.github.henriquemb.ticketsystem.database.controller.TicketController;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Задержка между обращениями и лимит обращений без ответа для каждого игрока.
 * Тип обращения: ticket, report или suggestion. Право ticketsystem.bypass.antispam отключает проверки.
 */
public final class AntiSpam {
    private static final Map<String, Map<UUID, Long>> LAST_SENT = new HashMap<>();

    private AntiSpam() {
    }

    /**
     * Проверяет ограничения и сам отправляет игроку сообщение, если обращение отклонено.
     */
    public static boolean check(Player p, String type) {
        if (p.hasPermission("ticketsystem.bypass.antispam")) return true;

        Settings.Limits limits = TicketSystem.getSettings().limits(type);

        Long last = LAST_SENT.getOrDefault(type, Map.of()).get(p.getUniqueId());
        if (last != null && limits.cooldownSeconds() > 0) {
            long left = last + limits.cooldownSeconds() * 1000L - System.currentTimeMillis();
            if (left > 0) {
                TicketSystem.getModel().sendMessage(p, TicketSystem.getMessages().getString("anti-spam.cooldown")
                        .replace("<time>", String.valueOf((left + 999) / 1000)), type);
                return false;
            }
        }

        if (limits.maxOpen() > 0 && countOpen(p.getName(), type) >= limits.maxOpen()) {
            TicketSystem.getModel().sendMessage(p, TicketSystem.getMessages().getString("anti-spam.max_open." + type)
                    .replace("<max>", String.valueOf(limits.maxOpen())), type);
            return false;
        }

        return true;
    }

    /**
     * Запоминает время успешно отправленного обращения.
     */
    public static void mark(Player p, String type) {
        LAST_SENT.computeIfAbsent(type, k -> new HashMap<>()).put(p.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Проверяет минимальное количество слов в тексте обращения.
     */
    public static boolean hasEnoughWords(String text, String type) {
        String trimmed = text.trim();
        int words = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
        return words >= TicketSystem.getSettings().limits(type).minWords();
    }

    private static int countOpen(String player, String type) {
        return switch (type) {
            case "ticket" -> new TicketController().countOpenByPlayer(player);
            case "report" -> new ReportController().countOpenByPlayer(player);
            case "suggestion" -> new SuggestionController().countOpenByPlayer(player);
            default -> 0;
        };
    }
}
