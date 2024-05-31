package net.bestemor.core.menu;

import net.bestemor.core.config.ConfigManager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Represents a {@link Clickable} with a static slot.
 * To be used for loading Clickables from config with configured slot.
 */
public class PlacedClickable extends Clickable {

    private final int slot;

    public PlacedClickable(int slot, ItemStack item, Consumer<InventoryClickEvent> consumer) {
        super(item, consumer);
        this.slot = slot;
    }

    public PlacedClickable(int slot, ItemStack item) {
        super(item);
        this.slot = slot;
    }

    /**
     * Creates a PlacedClickable from a config path
     * @param path path to item in config
     * @param onClick consumer to run on click
     * @return PlacedClickable
     */
    @SuppressWarnings("unused")
    public static PlacedClickable fromConfig(String path, Consumer<InventoryClickEvent> onClick) {
        int slot = ConfigManager.getInt(path + ".slot");
        return new PlacedClickable(slot, ConfigManager.getItem(path).build(), onClick);
    }

    /**
     * Creates a PlacedClickable from a config path with no consumer
     * @param path path to item in config
     * @return PlacedClickable with no consumer
     */
    @SuppressWarnings("unused")
    public static PlacedClickable fromConfig(String path) {
        int slot = ConfigManager.getInt(path + ".slot");
        return new PlacedClickable(slot, ConfigManager.getItem(path).build());
    }


    public int getSlot() {
        return slot;
    }
}
