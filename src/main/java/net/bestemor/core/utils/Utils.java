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

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean hasComma(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ',') {
                return true;
            }
        }
        return false;
    }
}
