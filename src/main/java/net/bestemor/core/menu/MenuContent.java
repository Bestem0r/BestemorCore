package net.bestemor.core.menu;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MenuContent {

    private final int size;
    private final Map<Integer, Clickable> clickables = new HashMap<>();

    private ItemStack lastFilledItem = null;

    public MenuContent(int size) {
        this.size = size;
    }

    /** Adds or replaces a {@link Clickable} in the inventory
     * @param slot inventory slot
     * @param clickable clickable associated with slot */
    public void setClickable(int slot, Clickable clickable) {
        this.clickables.put(slot, clickable);
    }

    /**
     * Adds or replaces a {@link PlacedClickable} in the inventory
     * @param placedClickable placed clickable associated with slot
     */
    @SuppressWarnings("unused")
    public void setPlaced(PlacedClickable placedClickable) {
        this.clickables.put(placedClickable.getSlot(), placedClickable);
    }

    /** Fills provided slots with item
     * @param item ItemStack to fill
     * @param slots Inventory slots to fill */
    @SuppressWarnings("unused")
    public void fillSlots(ItemStack item, int... slots) {
        for (int s : slots) {
            clickables.put(s, Clickable.empty(item));
        }
        this.lastFilledItem = item;
    }

    /** Fills edges of inventory with item
     * @param item ItemStack to fill */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
