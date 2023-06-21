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

import java.time.Instant;
import java.util.*;

public class MenuListener implements Listener {

    private final Plugin plugin;
    private final Set<Menu> menus = new HashSet<>();

    private final List<Menu> recentlyReopened = new ArrayList<>();

    public MenuListener(Plugin plugin) {
        this.plugin = plugin;
    }

    /** Registers menu
     * @param menu Menu to register **/
    public void registerMenu(Menu menu) {
        menus.add(menu);
    }

    /** Unregisters menu
     * @param menu Menu to unregister */
    @SuppressWarnings("unused")
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

        Instant reopenLimit = Instant.now().minusMillis(200);
        for (Menu menu : menus) {
            if (menu.getLastOpenedAt().isBefore(reopenLimit)) {
                continue;
            }
            if (recentlyReopened.contains(menu)) {
                continue;
            }
            if (!event.getView().getTitle().equals(menu.getTitle())) {
                continue;
            }
            recentlyReopened.add(menu);
            Bukkit.getScheduler().runTaskLater(plugin, () -> menu.open((Player) event.getPlayer()), 1L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> recentlyReopened.remove(menu), 20L);
            return;
        }

        menus.stream().sorted(Comparator.comparing(Menu::getLastOpenedAt))
                .filter(menu -> menu.hasPlayer(event.getPlayer()))
                .findFirst().ifPresent(menu -> menu.onClose(event));

        Instant limit = Instant.now().minusSeconds(5);
        menus.removeIf(menu -> menu.getViewers().size() == 0 && menu.getLastOpenedAt().isBefore(limit));
    }


    /** Closes all currently opened menus  */
    public void closeAll() {
        List<Menu> menusCopy = new ArrayList<>(menus);
        menusCopy.forEach(Menu::close);
    }

    /** Returns all currently registered menus */
    @SuppressWarnings("unused")
    public Collection<Menu> getRegisteredMenus() {
        return new ArrayList<>(menus);
    }
}
