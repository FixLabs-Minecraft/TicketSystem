package com.github.henriquemb.ticketsystem.commands;

import com.github.henriquemb.ticketsystem.Model;
import com.github.henriquemb.ticketsystem.TicketSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class TicketSystemCommand implements CommandExecutor, TabCompleter {
    private final Model m = TicketSystem.getModel();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ticketsystem.admin")) {
            m.sendMessage(sender, TicketSystem.getMessages().getString("permission.no_permission"));
            return true;
        }

        TicketSystem.getMain().reload();

        TicketSystem.getModel().sendMessage(sender, TicketSystem.getMessages().getString("reload"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length <= 1 ? List.of("reload") : List.of();
    }
}
