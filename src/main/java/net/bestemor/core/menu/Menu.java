package net.bestemor.core.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Menu {

    private final MenuListener listener;

    private final MenuContent content;
    private final Inventory inventory;

    private final Map<Integer, Clickable> clickables = new HashMap<>();

    private boolean isCreated = false;

    protected Menu(MenuListener listener, int size, String name) {
        this.listener = listener;
        this.content = new MenuContent(size);

        this.inventory = Bukkit.createInventory(null, size, name);
    }

    protected void onClick(InventoryClickEvent event) {}
    protected void onClose(InventoryCloseEvent event) {}

    public void update() {
        onUpdate(content);
        loadContentToInventory();
    }

    protected void onUpdate(MenuContent content) {
        create();
    };

    protected abstract void onCreate(MenuContent content);

    private void create() {
        inventory.clear();
        onCreate(content);
        loadContentToInventory();
    }

    public void open(Player player) {
        if (!isCreated) {
            create();
            isCreated = true;
        }
        player.openInventory(inventory);
        this.listener.registerMenu(this);
    }

    private void loadContentToInventory() {
        for (Integer slot : content.getClickables().keySet()) {
            inventory.setItem(slot, content.getClickables().get(slot).getItem());
        }
    }

    public boolean hasPlayer(HumanEntity entity) {
        return inventory.getViewers().stream()
                .map(Entity::getUniqueId)
                .collect(Collectors.toList())
                .contains(entity.getUniqueId());
    }

    public List<HumanEntity> getViewers() {
        return inventory == null ? new ArrayList<>() : inventory.getViewers();
    }

    public void close() {
        List<HumanEntity> viewers = new ArrayList<>(getViewers());
        viewers.forEach(HumanEntity::closeInventory);
    }

    protected Map<Integer, Clickable> getClickables() {
        return clickables;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
