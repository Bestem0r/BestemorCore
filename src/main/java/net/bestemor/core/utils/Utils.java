package net.bestemor.core.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class Utils {
    public static String PAPIParse(String str, OfflinePlayer player)
    {
        if(Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceHolderAPI"))
            return PlaceholderAPI.setPlaceholders(player, str);
        return str;
    }
    public static String PAPIParse(String str)
    {
        return PAPIParse(str, null);
    }
}
