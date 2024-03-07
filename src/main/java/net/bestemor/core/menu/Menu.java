package net.bestemor.core.menu;

import net.bestemor.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.time.Instant;
import java.util.*;

public abstract class Menu {

    private final MenuListener listener;

    private final MenuContent content;
    private final Inventory inventory;

    private boolean isCreated = false;
    private final String title;

    private final Map<UUID, Instant> lastOpenedAt = new HashMap<>();

    public Menu(MenuListener listener, int size, String name) {
        this.listener = listener;
        this.content = new MenuContent(size);

        this.title = Utils.parsePAPI(name);
        this.inventory = Bukkit.createInventory(null, size, title);
    }

    protected void onClick(InventoryClickEvent event) {}
    @SuppressWarnings("unused")
    protected void onDrag(InventoryDragEvent event) {}
    protected void onClose(InventoryCloseEvent event) {}

    /** Updates menu and applies clickables from MenuContent */
    public void update() {
        onUpdate(content);
        for (Integer slot : content.getClickables().keySet()) {
            if (content.getClickables().get(slot) == null) {
                inventory.setItem(slot, null);
            } else {
                inventory.setItem(slot, content.getClickables().get(slot).getItem());
            }
        }
    }

    protected void onUpdate(MenuContent content) {}

    /** Runs when menu is initially created
     * @param content Container of Clickables which is applied to the inventory */
    protected abstract void onCreate(MenuContent content);

    /** Forcibly creates and updates the menu */
    public void create() {
        inventory.clear();
        onCreate(content);
        update();
        isCreated = true;
    }

    /** Creates and updates the menu if opened for the first time,
     * then opens the menu for the player
     * @param player Player to open the menu for */
    public void open(Player player) {
        if (!isCreated) {
            create();
        }
        lastOpenedAt.put(player.getUniqueId(), Instant.now());
        player.openInventory(inventory);
        this.listener.registerMenu(this);
    }

    /** @return  Whether specified the player currently has this menu opened */
    public boolean hasPlayer(HumanEntity entity) {
        return inventory.getViewers().stream()
                .map(Entity::getUniqueId)
                .anyMatch(uuid -> uuid.equals(entity.getUniqueId()));
    }

    /** @return List of current players viewing this menu */
    public List<HumanEntity> getViewers() {
        return inventory == null ? new ArrayList<>() : inventory.getViewers();
    }

    /** Closes this menu for all active viewers */
    public void close() {
        List<HumanEntity> viewers = new ArrayList<>(getViewers());
        viewers.forEach(HumanEntity::closeInventory);
    }

    @SuppressWarnings("unused")
    protected void close(HumanEntity player) {
        inventory.getViewers().removeIf(v -> v.getUniqueId().equals(player.getUniqueId()));
        lastOpenedAt.remove(player.getUniqueId());
    }

    /** @return Content containing clickables for this menu */
    public MenuContent getContent() {
        return content;
    }

    /** @return The raw inventory used for this menu */
    public Inventory getInventory() {
        return inventory;
    }

    public String getTitle() {
        return title;
    }

    /** @return The time this menu was last opened by the player */
    public Instant getLastOpenedAt(UUID uuid) {
        return lastOpenedAt.getOrDefault(uuid, Instant.MIN);
    }
}
