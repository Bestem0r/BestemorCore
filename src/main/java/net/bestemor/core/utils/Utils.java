package net.bestemor.core.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public final class Utils {

    private Utils() {}

    public static String parsePAPI(String str, OfflinePlayer player) {
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceHolderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, str);
        }
        return str;
    }
    public static String parsePAPI(String str) {
        return parsePAPI(str, null);
    }
}
