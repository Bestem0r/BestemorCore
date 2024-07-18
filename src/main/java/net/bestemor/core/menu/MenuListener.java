package net.bestemor.core.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.stream.Collectors;

public class MenuListener implements Listener {

    private final Map<UUID, OpenedMenu> openMenus = new HashMap<>();

    /** Registers menu
     * @param menu Menu to register **/
    public void registerMenu(UUID player, Menu menu) {
        openMenus.put(player, new OpenedMenu(menu));
    }

    /** Unregisters menu
     * @param player The UUID of the player to unregister **/
    @SuppressWarnings("unused")
    public void unregisterMenu(UUID player) {
        openMenus.remove(player);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();

        OpenedMenu openedMenu = openMenus.get(player.getUniqueId());
        if (openedMenu != null) {
            Menu menu = openedMenu.getMenu();
            int slot = event.getRawSlot();
            event.setCancelled(true);
            menu.onClick(event);
            if (menu.getContent().getClickables().containsKey(slot) && menu.getContent().getClickables().get(slot) != null) {
                menu.getContent().getClickables().get(slot).onClick(event);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onDrag(InventoryDragEvent event) {

        Player player = (Player) event.getWhoClicked();
        OpenedMenu openedMenu = openMenus.get(player.getUniqueId());
        if (openedMenu != null) {
            event.setCancelled(true);
            openedMenu.getMenu().onDrag(event);
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onClose(InventoryCloseEvent event) {

        UUID playerUUID = event.getPlayer().getUniqueId();

        OpenedMenu openedMenu = openMenus.get(playerUUID);

        if (openedMenu == null) {
            return;
        }

        if (!openedMenu.getMenu().getTitle().equals(event.getView().getTitle())) {
            return;
        }
        openMenus.remove(playerUUID);
        openedMenu.menu.onClose(event);
    }



    @EventHandler (priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        OpenedMenu openedMenu = openMenus.get(playerUUID);
        if (openedMenu != null) {
            openedMenu.menu.onClose(new InventoryCloseEvent(event.getPlayer().getOpenInventory()));
            openMenus.remove(playerUUID);
        }
    }


    /** Closes all currently opened menus  */
    public void closeAll() {
        List<Menu> menus = new ArrayList<>(openMenus.values()).stream()
                .filter(Objects::nonNull)
                .map(OpenedMenu::getMenu).collect(Collectors.toList());
        menus.forEach(Menu::close);
    }

    /** Returns all currently registered menus */
    @SuppressWarnings("unused")
    public Collection<Menu> getRegisteredMenus() {
        return openMenus.values().stream().map(OpenedMenu::getMenu).collect(Collectors.toList());
    }

    /** @return Whether the player currently has a specified {@link Menu} opened
     * @param player Player to check **/
    public boolean hasMenu(Player player, Menu menu) {
        return openMenus.containsKey(player.getUniqueId()) && openMenus.get(player.getUniqueId()).getMenu() == menu;
    }

    private static class OpenedMenu {
        private final Menu menu;

        private OpenedMenu(Menu menu) {
            this.menu = menu;
        }

        public Menu getMenu() {
            return menu;
        }
    }
}
