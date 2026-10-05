package com.github.henriquemb.ticketsystem.gui;

import com.github.henriquemb.ticketsystem.TicketSystem;
import com.github.henriquemb.ticketsystem.database.controller.TicketController;
import com.github.henriquemb.ticketsystem.database.model.TicketModel;
import com.github.henriquemb.ticketsystem.enums.TicketRatingEnum;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * Тикеты самого игрока. Клик — открыть в чате (там же оценка ответа).
 */
public class MyTicketsMenu extends ListMenu<TicketModel> {
    public MyTicketsMenu(Player viewer, int page) {
        super(viewer, "my-tickets", page);
    }

    @Override
    protected List<TicketModel> load() {
        return new TicketController().fetchByPlayer(viewer.getName());
    }

    @Override
    protected ItemStack icon(TicketModel ticket) {
        boolean answered = ticket.getResponse() != null;
        String rating = Arrays.stream(TicketRatingEnum.values())
                .filter(r -> r.getRate() == ticket.getRating())
                .findFirst()
                .map(TicketRatingEnum::format)
                .orElse("");

        Icons icons = new Icons()
                .with("id", ticket.getId())
                .with("date", Icons.formatDate(ticket.getTimestamp()))
                .with("state", TicketSystem.getMessages().getString(answered ? "ticket.my.state.answered" : "ticket.my.state.pending"))
                .with("respondedBy", ticket.getRespondedBy())
                .with("rating", rating)
                .text("text", ticket.getRequest())
                .text("response", ticket.getResponse());

        return answered
                ? icons.build(TicketSystem.getSettings().icon("my-ticket-answered", Material.ENCHANTED_BOOK), "gui.item.my-ticket-answered")
                : icons.build(TicketSystem.getSettings().icon("my-ticket-pending", Material.BOOK), "gui.item.my-ticket-pending");
    }

    @Override
    protected void click(TicketModel ticket, ClickType click) {
        runCommand("ticket view " + ticket.getId());
    }
}
