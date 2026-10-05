package com.github.henriquemb.ticketsystem.gui;

import com.github.henriquemb.ticketsystem.TicketSystem;
import com.github.henriquemb.ticketsystem.database.controller.SuggestionController;
import com.github.henriquemb.ticketsystem.database.model.SuggestionModel;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Предложения без ответа. Клик — открыть в чате с кнопкой «Ответить».
 */
public class SuggestionsMenu extends ListMenu<SuggestionModel> {
    public SuggestionsMenu(Player viewer, int page) {
        super(viewer, "suggestions", page);
    }

    @Override
    protected List<SuggestionModel> load() {
        return new SuggestionController().fetchNotAnswered();
    }

    @Override
    protected ItemStack icon(SuggestionModel suggestion) {
        return new Icons()
                .with("id", suggestion.getId())
                .with("player", suggestion.getPlayer())
                .with("date", Icons.formatDate(suggestion.getTimestamp()))
                .text("text", suggestion.getSuggestion())
                .build(TicketSystem.getSettings().icon("suggestion", Material.WRITABLE_BOOK), "gui.item.suggestion");
    }

    @Override
    protected void click(SuggestionModel suggestion, ClickType click) {
        runCommand("suggestion view " + suggestion.getId());
    }
}
