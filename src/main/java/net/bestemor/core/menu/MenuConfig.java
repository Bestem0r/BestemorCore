package net.bestemor.core.menu;

import net.bestemor.core.config.ConfigManager;

/**
 * Represents a configuration for a menu.
 */
public class MenuConfig {

    private final int size;
    private final String title;

    private MenuConfig(int size, String title) {
        this.size = size;
        this.title = title;
    }

    /**
     * Contructs a MenuConfig from a config path
     * @param path path to menu config
     * @return MenuConfig
     */
    public static MenuConfig fromConfig(String path) {
        int size = ConfigManager.getInt(path + ".size");
        String title = ConfigManager.getString(path + ".title");
        return new MenuConfig(size, title);
    }

    public int getSize() {
        return size;
    }

    public String getTitle() {
        return title;
    }
}
