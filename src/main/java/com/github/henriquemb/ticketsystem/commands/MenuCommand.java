package com.github.henriquemb.ticketsystem.commands;

import com.github.henriquemb.ticketsystem.TicketSystem;
import com.github.henriquemb.ticketsystem.gui.MainMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MenuCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(TicketSystem.getMessages().getString("warnings.only_players"));
            return true;
        }

        open(p);
        return true;
    }

    public static void open(Player p) {
        if (!TicketSystem.getSettings().isGuiEnabled()) {
            TicketSystem.getModel().sendMessage(p, TicketSystem.getMessages().getString("gui.disabled"), "ticket");
            return;
        }

        new MainMenu(p).open();
    }
}
