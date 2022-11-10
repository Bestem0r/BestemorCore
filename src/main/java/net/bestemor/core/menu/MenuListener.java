package net.bestemor.core.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MenuListener implements Listener {

    private final Plugin plugin;
    private final List<Menu> menus = new ArrayList<>();

    public MenuListener(Plugin plugin) {
        this.plugin = plugin;
    }

    /** Registers menu one tick later
     * @param menu Menu to register **/
    public void registerMenu(Menu menu) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> menus.add(menu), 1L);
    }

    /** Unregisters menu
     * @param menu Menu to unregister */
    public void unregisterMenu(Menu menu) {
        menus.remove(menu);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();

        List<Menu> menusCopy = new ArrayList<>(menus);
        for (Menu menu : menusCopy) {
            if (menu.hasPlayer(player)) {
                int slot = event.getRawSlot();
                event.setCancelled(true);
                menu.onClick(event);
                if (menu.getContent().getClickables().containsKey(slot) && menu.getContent().getClickables().get(slot) != null) {
                    menu.getContent().getClickables().get(slot).onClick(event);
                }
                break;
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onDrag(InventoryDragEvent event) {
        for (Menu menu : menus) {
            if (menu.hasPlayer(event.getWhoClicked())) {
                menu.onDrag(event);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onClose(InventoryCloseEvent event) {
        for (Menu menu : menus) {
            if (menu.hasPlayer(event.getPlayer())) {
                menu.onClose(event);
                menu.close(event.getPlayer());
            }
        }
        menus.removeIf(menu -> menu.getViewers().size() < 1 || (menu.getViewers().size() == 1 && menu.hasPlayer(event.getPlayer())));
    }


    /** Closes all currently opened menus  */
    public void closeAll() {
        List<Menu> menusCopy = new ArrayList<>(menus);
        menusCopy.forEach(Menu::close);
    }

    /** Returns all currently registered menus */
    public Collection<Menu> getRegisteredMenus() {
        return new ArrayList<>(menus);
    }
}
