package net.bestemor.core;

import net.bestemor.core.config.ConfigManager;
import net.bestemor.core.config.VersionUtils;
import net.bestemor.core.menu.MenuListener;
import net.bestemor.core.utils.UpdateChecker;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        ConfigManager.setConfig(getConfig());
        getConfig().options().copyDefaults(true);

        if (getLanguageFolder() != null) {
            ConfigManager.setLanguagesFolder(new File(getDataFolder(), getLanguageFolder()));
            ConfigManager.loadLanguages(this, getLanguages());
        }

        if (getSpigotResourceID() != 0) {
            checkVersion();
        }

        onPluginEnable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.menuListener.closeAll();
        Bukkit.getScheduler().cancelTasks(this);
        onPluginDisable();
    }

    private void checkVersion() {
        int resourceId = getSpigotResourceID();
        new UpdateChecker(this, resourceId).getVersion(version -> {
            String currentVersion = this.getDescription().getVersion();
            if (!currentVersion.equalsIgnoreCase(version)) {
                String foundVersion = ChatColor.AQUA + "A new version of " + getDescription().getName() + " was found!";
                String latestVersion = ChatColor.AQUA + "Latest version: " + ChatColor.GREEN + version;
                String yourVersion = ChatColor.AQUA + "Your version: " + ChatColor.RED + currentVersion;
                String downloadVersion = ChatColor.AQUA + "Get it here for the latest features and bug fixes: " + ChatColor.YELLOW + "https://www.spigotmc.org/resources/" + resourceId + "/";

                getLogger().warning(foundVersion);
                getLogger().warning(latestVersion);
                getLogger().warning(yourVersion);
                getLogger().warning(downloadVersion);
            }
        });
    }

    protected abstract void onPluginEnable();

    protected void onPluginDisable() {}

    protected String[] getLanguages() {
        return new String[]{};
    }

    protected String getLanguageFolder() {
        return null;
    }

    protected int getSpigotResourceID() {
        return 0;
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
