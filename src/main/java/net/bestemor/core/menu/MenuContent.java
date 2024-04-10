package net.bestemor.core.menu;

import net.bestemor.core.config.ConfigManager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MenuContent {

    private final int size;
    private final Map<Integer, Clickable> clickables = new HashMap<>();

    private ItemStack lastFilledItem = null;

    public MenuContent(int size) {
        this.size = size;
    }

    /** Adds or replaces clickable inventory slot
     * @param slot Inventory slot
     * @param clickable Clickable associated with slot */
    public void setClickable(int slot, Clickable clickable) {
        this.clickables.put(slot, clickable);
    }

    /** Loads item from config and creates clickable, adding it
     * to the inventory at the config specified slot
     * @param path Config path to item
     * @param onClick Consumer to run on click
     */
    public void addConfigClickable(String path, Consumer<InventoryClickEvent> onClick) {
        Clickable clickable = new Clickable(ConfigManager.getItem(path).build(), onClick);
        int slot = ConfigManager.getInt(path + ".slot");
        setClickable(slot, clickable);
    }

    /** Loads item from config and creates clickable, adding it
     * to the inventory at the specified slot
     * @param slot Inventory slot
     * @param path Config path to item
     * @param onClick Consumer to run on click
     */
    public void setConfigClickable(int slot, String path, Consumer<InventoryClickEvent> onClick) {
        Clickable clickable = new Clickable(ConfigManager.getItem(path).build(), onClick);
        setClickable(slot, clickable);
    }

    /** Fills provided slots with item
     * @param item ItemStack to fill
     * @param slots Inventory slots to fill */
    public void fillSlots(ItemStack item, int... slots) {
        for (int s : slots) {
            clickables.put(s, Clickable.empty(item));
        }
        this.lastFilledItem = item;
    }

    /** Fills edges of inventory with item
     * @param item ItemStack to fill */
    public void fillEdges(ItemStack item) {
        for (int s = 0; s < size; s++) {
            if (s < 9 || s > size - 9 || (s % 9 == 0) || ((s + 1) % 9 == 0)) {
                clickables.put(s, new Clickable(item));
            }
        }
        this.lastFilledItem = item;
    }

    /** Fills bottom of inventory with item
     * @param item ItemStack to fill */
    public void fillBottom(ItemStack item) {
        for (int s = size - 9; s < size; s++) {
            clickables.put(s, Clickable.empty(item));
        }
        this.lastFilledItem = item;
    }

    protected ItemStack getLastFilledItem() {
        return lastFilledItem;
    }

    protected Map<Integer, Clickable> getClickables() {
        return clickables;
    }
}
