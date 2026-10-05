package com.github.henriquemb.ticketsystem.gui;

import com.github.henriquemb.ticketsystem.TicketSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Базовое меню плагина. Все клики внутри меню отменяются и передаются в {@link #onClick}.
 */
public abstract class Menu implements InventoryHolder {
    protected final Player viewer;
    protected Inventory inventory;

    protected Menu(Player viewer) {
        this.viewer = viewer;
    }

    /**
     * Заполняет {@link #inventory}. Вызывается при каждом открытии меню.
     */
    protected abstract void build();

    public abstract void onClick(int slot, ClickType click);

    public void open() {
        build();
        viewer.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    /**
     * Закрывает меню и выполняет команду от имени игрока на следующем тике.
     */
    protected void runCommand(String command) {
        viewer.closeInventory();
        Bukkit.getScheduler().runTask(TicketSystem.getMain(), () -> viewer.performCommand(command));
    }

    /**
     * Открывает другое меню на следующем тике (нельзя открывать инвентарь прямо из события клика).
     */
    protected void openLater(Menu menu) {
        Bukkit.getScheduler().runTask(TicketSystem.getMain(), menu::open);
    }
}
