package com.github.henriquemb.ticketsystem.gui;

import com.github.henriquemb.ticketsystem.TicketSystem;
import com.github.henriquemb.ticketsystem.database.controller.ReportController;
import com.github.henriquemb.ticketsystem.database.controller.SuggestionController;
import com.github.henriquemb.ticketsystem.database.controller.TicketController;
import com.github.henriquemb.ticketsystem.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Главное меню. Игроки видят «Задать вопрос» и «Мои тикеты», персонал — ещё и очереди
 * тикетов, жалоб и предложений (в зависимости от прав).
 */
public class MainMenu extends Menu {
    private static final int[][] LAYOUT = {
            {13}, {11, 15}, {11, 13, 15}, {10, 12, 14, 16}, {9, 11, 13, 15, 17}
    };

    private final Map<Integer, Runnable> actions = new HashMap<>();

    public MainMenu(Player viewer) {
        super(viewer);
    }

    @Override
    protected void build() {
        inventory = Bukkit.createInventory(this, 27, Text.parse(TicketSystem.getMessages().getString("gui.title.main")));
        actions.clear();

        Map<String, Runnable> buttons = new LinkedHashMap<>();
        Map<String, Integer> counts = new HashMap<>();

        if (viewer.hasPermission("ticketsystem.ticket.use")) {
            buttons.put("ask", this::ask);
            buttons.put("my-tickets", () -> openLater(new MyTicketsMenu(viewer, 1)));
            counts.put("my-tickets", new TicketController().fetchByPlayer(viewer.getName()).size());
        }
        if (viewer.hasPermission("ticketsystem.ticket.staff")) {
            buttons.put("tickets", () -> openLater(new TicketsMenu(viewer, 1)));
            counts.put("tickets", new TicketController().countNotAnswered());
        }
        if (viewer.hasPermission("ticketsystem.report.staff")) {
            buttons.put("reports", () -> openLater(new ReportsMenu(viewer, 1)));
            counts.put("reports", new ReportController().fetchNotVerified().size());
        }
        if (viewer.hasPermission("ticketsystem.suggestion.staff")) {
            buttons.put("suggestions", () -> openLater(new SuggestionsMenu(viewer, 1)));
            counts.put("suggestions", new SuggestionController().fetchNotAnswered().size());
        }

        if (buttons.isEmpty()) return;

        int[] slots = LAYOUT[buttons.size() - 1];
        int i = 0;
        for (Map.Entry<String, Runnable> button : buttons.entrySet()) {
            String key = button.getKey();
            inventory.setItem(slots[i], new Icons()
                    .with("count", counts.getOrDefault(key, 0))
                    .build(TicketSystem.getSettings().icon(key, defaultIcon(key)), "gui.main." + key));
            actions.put(slots[i], button.getValue());
            i++;
        }
    }

    private static Material defaultIcon(String key) {
        return switch (key) {
            case "ask" -> Material.OAK_SIGN;
            case "my-tickets" -> Material.BOOK;
            case "tickets" -> Material.PAPER;
            case "reports" -> Material.BELL;
            default -> Material.WRITABLE_BOOK;
        };
    }

    private void ask() {
        viewer.closeInventory();
        TicketSystem.getModel().sendMessage(viewer, TicketSystem.getMessages().getString("gui.ask-hint"), "ticket");
    }

    @Override
    public void onClick(int slot, ClickType click) {
        Runnable action = actions.get(slot);
        if (action != null) action.run();
    }
}
