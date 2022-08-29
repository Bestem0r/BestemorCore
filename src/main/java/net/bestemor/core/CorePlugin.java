package net.bestemor.core;

import net.bestemor.core.config.ConfigManager;
import net.bestemor.core.config.VersionUtils;
import net.bestemor.core.menu.MenuListener;
import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public abstract class CorePlugin extends JavaPlugin {

    private MenuListener menuListener;

    @Override
    public void onEnable() {

        this.menuListener = new MenuListener(this);
        getServer().getPluginManager().registerEvents(menuListener, this);

        if (!new File(getDataFolder() + "/config.yml").exists()) {
            if (VersionUtils.getMCVersion() < 13) {

                InputStream versionConfig = getResource("config_" + VersionUtils.getMCVersion() + ".yml");
                versionConfig = versionConfig == null ? getResource("config_legacy") : versionConfig;

                if (versionConfig == null) {
                    saveDefaultConfig();
                    return;
                }

                File target = new File(getDataFolder() + "/config.yml");
                try {
                    FileUtils.copyInputStreamToFile(Objects.requireNonNull(versionConfig), target);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                saveDefaultConfig();
            }
        }
        getConfig().options().copyDefaults(true);

        ConfigManager.setConfig(getConfig());
        if (getLanguageFolder() != null) {
            ConfigManager.setLanguagesFolder(new File(getDataFolder(), getLanguageFolder()));
            ConfigManager.loadLanguages(this, getLanguages());
        }

        onPluginEnable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.menuListener.closeAll();
        onPluginDisable();
    }

    protected abstract void onPluginEnable();

    protected void onPluginDisable() {};

    protected String[] getLanguages() {
        return new String[]{};
    }

    protected String getLanguageFolder() {
        return null;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        ConfigManager.clearCache();
        ConfigManager.setConfig(getConfig());
    }

    public MenuListener getMenuListener() {
        return menuListener;
    }
}
