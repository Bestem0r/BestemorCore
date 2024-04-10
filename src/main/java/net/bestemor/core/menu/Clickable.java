package net.bestemor.core.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

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

    public static Clickable of(ItemStack item, Consumer<InventoryClickEvent> consumer) {
        return new Clickable(item, consumer);
    }

    public static Clickable empty(ItemStack item) {
        return new Clickable(item);
    }

    @SuppressWarnings("unused")
    public boolean isClickable() {
        return this.consumer != null;
    }

    public void onClick(InventoryClickEvent event) {
        if (this.consumer != null) {
            consumer.accept(event);
        }
    }

    public ItemStack getItem() {
        return item;
    }
}
