package net.bestemor.core.menu;

import net.bestemor.core.config.ConfigManager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Represents a clickable item in a {@link MenuContent}
 */
public class Clickable {

    private final ItemStack item;
    private final Consumer<InventoryClickEvent> consumer;

    public Clickable(ItemStack item, Consumer<InventoryClickEvent> consumer) {
        this.item = item;
        this.consumer = consumer;
    }

    public Clickable(ItemStack item) {
        this.item = item;
        this.consumer = null;
    }

    /**
     * Creates a clickable item with a consumer
     * @param item item to place
     * @param consumer consumer to run on click
     * @return Clickable
     */
    public static Clickable of(ItemStack item, Consumer<InventoryClickEvent> consumer) {
        return new Clickable(item, consumer);
    }

    /**
     * Creates a clickable item without a consumer
     * @param item item to place
     * @return Clickable with no consumer
     */
    public static Clickable empty(ItemStack item) {
        return new Clickable(item);
    }

    /**
     * Creates a clickable item from a config path
     * @param path path to item in config
     * @param onClick consumer to run on click
     * @return Clickable
     */
    @SuppressWarnings("unused")
    public static Clickable fromConfig(String path, Consumer<InventoryClickEvent> onClick) {
        return new Clickable(ConfigManager.getItem(path).build(), onClick);
    }

    /**
     * Creates a clickable item from a config path with no consumer
     * @param path path to item in config
     * @return Clickable with no consumer
     */
    @SuppressWarnings("unused")
    public static Clickable fromConfig(String path) {
        return new Clickable(ConfigManager.getItem(path).build());
    }

    public boolean isClickable() {
        return this.consumer != null;
    }

    public void onClick(InventoryClickEvent event) {
        if (isClickable()) {
            consumer.accept(event);
        }
    }

    public ItemStack getItem() {
        return item;
    }
}
