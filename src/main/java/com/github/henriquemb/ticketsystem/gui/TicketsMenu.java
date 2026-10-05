package com.github.henriquemb.ticketsystem.gui;

import com.github.henriquemb.ticketsystem.TicketSystem;
import com.github.henriquemb.ticketsystem.database.controller.TicketController;
import com.github.henriquemb.ticketsystem.database.model.TicketModel;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Тикеты, ожидающие ответа. ЛКМ — открыть в чате, ПКМ — телепорт к автору.
 */
public class TicketsMenu extends ListMenu<TicketModel> {
    public TicketsMenu(Player viewer, int page) {
        super(viewer, "tickets", page);
    }

    @Override
    protected List<TicketModel> load() {
        return new TicketController().fetchNotAnswered();
    }

    @Override
    protected ItemStack icon(TicketModel ticket) {
        return new Icons()
                .with("id", ticket.getId())
                .with("player", ticket.getPlayer())
                .with("date", Icons.formatDate(ticket.getTimestamp()))
                .text("text", ticket.getRequest())
                .build(TicketSystem.getSettings().icon("ticket", Material.PAPER), "gui.item.ticket");
    }

    @Override
    protected void click(TicketModel ticket, ClickType click) {
        runCommand((click.isRightClick() ? "ticket teleport " : "ticket view ") + ticket.getId());
    }
}
