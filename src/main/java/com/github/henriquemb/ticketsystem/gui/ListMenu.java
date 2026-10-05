package com.github.henriquemb.ticketsystem.gui;

import com.github.henriquemb.ticketsystem.TicketSystem;
import com.github.henriquemb.ticketsystem.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Список с постраничной навигацией: 45 слотов под элементы и нижняя панель.
 */
public abstract class ListMenu<T> extends Menu {
    private static final int PAGE_SIZE = 45;
    private static final int SLOT_PREVIOUS = 45;
    private static final int SLOT_BACK = 49;
    private static final int SLOT_NEXT = 53;

    private final String titleKey;
    private int page;
    private List<T> items = List.of();

    protected ListMenu(Player viewer, String titleKey, int page) {
        super(viewer);
        this.titleKey = titleKey;
        this.page = page;
    }

    protected abstract List<T> load();

    protected abstract ItemStack icon(T item);

    protected abstract void click(T item, ClickType click);

    @Override
    protected void build() {
        items = load();
        int pages = Math.max(1, (items.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.clamp(page, 1, pages);

        String title = TicketSystem.getMessages().getString("gui.title." + titleKey, titleKey)
                .replace("<page>", String.valueOf(page))
                .replace("<pages>", String.valueOf(pages));
        inventory = Bukkit.createInventory(this, 54, Text.parse(title));

        int from = (page - 1) * PAGE_SIZE;
        for (int i = from; i < Math.min(from + PAGE_SIZE, items.size()); i++) {
            inventory.setItem(i - from, icon(items.get(i)));
        }

        if (items.isEmpty())
            inventory.setItem(22, new Icons().build(TicketSystem.getSettings().icon("empty", Material.BARRIER), "gui.empty"));

        ItemStack filler = new Icons().build(TicketSystem.getSettings().icon("filler", Material.GRAY_STAINED_GLASS_PANE), "gui.filler");
        for (int slot = 45; slot < 54; slot++) inventory.setItem(slot, filler);

        if (page > 1)
            inventory.setItem(SLOT_PREVIOUS, new Icons().with("page", page - 1)
                    .build(TicketSystem.getSettings().icon("previous", Material.ARROW), "gui.buttons.previous"));
        if (page < pages)
            inventory.setItem(SLOT_NEXT, new Icons().with("page", page + 1)
                    .build(TicketSystem.getSettings().icon("next", Material.ARROW), "gui.buttons.next"));
        inventory.setItem(SLOT_BACK, new Icons().build(TicketSystem.getSettings().icon("back", Material.DARK_OAK_DOOR), "gui.buttons.back"));
    }

    @Override
    public void onClick(int slot, ClickType click) {
        switch (slot) {
            case SLOT_PREVIOUS -> {
                if (page > 1) reopen(page - 1);
            }
            case SLOT_NEXT -> reopen(page + 1);
            case SLOT_BACK -> openLater(new MainMenu(viewer));
            default -> {
                int index = (page - 1) * PAGE_SIZE + slot;
                if (slot < PAGE_SIZE && index < items.size()) click(items.get(index), click);
            }
        }
    }

    private void reopen(int newPage) {
        page = newPage;
        openLater(this);
    }
}
