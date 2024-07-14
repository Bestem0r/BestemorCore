package net.bestemor.core.config;

import net.bestemor.core.CorePlugin;
import net.bestemor.core.config.updater.ConfigUpdater;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Static utility class used to quickly retrieve values from default configuration
 * and language configurations. Supports auto-updating configs, per-version configs,
 * value mappings, and easy language file implementation. Automatically initiated by
 * extending {@link CorePlugin}. Otherwise, run {@link #setConfig(FileConfiguration)}
 * inside your plugin implementation. */
public abstract class ConfigManager {

    private static FileConfiguration config;
    private static String prefixPath = "prefix";
    private static String currencyPath = "currency";
    private static String isBeforePath = "currency_before";
    private static String languagePath = "language";
    private static boolean addPrefixSpace = true;

    private static File languagesFolder = null;
    private static FileConfiguration languageConfig = null;

    private final static Map<String, String> stringMappings = new HashMap<>();
    private final static Map<String, Object> cache = new HashMap<>();
    private final static Map<String, List<String>> listCache = new HashMap<>();

    private static final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-f])");

    private ConfigManager() {}

    /** Sets default config used by ConfigManager
     * @param config Default configuration */
    public static void setConfig(FileConfiguration config) {
        ConfigManager.config = config;
    }

    /** Adds missing config values to default configuration from included
     * config file in plugin .jar.
     * @param plugin Plugin to load default config from. If no config is previously
     * set, the config from this plugin will be set as the config used by ConfigManager. */
    public static void updateConfig(JavaPlugin plugin, String resource) {
        if (config == null) {
            setConfig(plugin.getConfig());
        }
        try {
            ConfigUpdater.update(plugin, resource + ".yml", new File(plugin.getDataFolder() + "/config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Clears cached config values. */
    public static void clearCache() {
        cache.clear();
        listCache.clear();
        if (config != null && languagePath != null && languagesFolder != null) {
            loadLanguageFile();
        }
    }

    /** Sets path used to retrieve plugin prefix used in messages. */
    @SuppressWarnings("unused")
    public static void setPrefixPath(String path) {
        prefixPath = path;
    }

    /** Sets path used for currency symbol. */
    @SuppressWarnings("unused")
    public static void setCurrencyPath(String currencyPath) {
        ConfigManager.currencyPath = currencyPath;
    }

    /** Sets path used to determine if currency symbol should be before value. */
    @SuppressWarnings("unused")
    public static void setIsBeforePath(String isBeforePath) {
        ConfigManager.isBeforePath = isBeforePath;
    }

    public static String getString(String path) {
        String s = get(path, String.class);
        if (s != null && stringMappings.containsKey(s)) {
            s = stringMappings.get(s);
        }
        return s == null ? path : translateColor(s);
    }

    /** Looks up a string in the loaded config mappings (config_mappings.yml)
     * and returns the mapped value. If no mapping is found, returns the input.
     * @return Mapped string. */
    @SuppressWarnings("unused")
    public static String getMappedString(String s) {
        return s == null ? null : stringMappings.getOrDefault(s, s);
    }

    /** @return Colored string with plugin prefix */
    public static String getMessage(String path) {
        if (prefixPath == null || getString(prefixPath).isEmpty()) {
            return getString(path);
        }
        return prefixPath == null ? "" : (getString(prefixPath) + (addPrefixSpace ? " " : "")) + getString(path);
    }

    @SuppressWarnings("unused")
    public static Sound getSound(String path) {
        return Sound.valueOf(getString(path));
    }

    public static boolean getBoolean(String path) {
        Boolean b = get(path, Boolean.class);
        return b != null && b;
    }

    @SuppressWarnings("unused")
    public static double getDouble(String path) {
        return config.getDouble(path);
    }

    public static int getInt(String path) {
        Integer i = get(path, Integer.class);
        return i == null ? 0 : i;
    }

    @SuppressWarnings("unused")
    public static long getLong(String path) {
        Long l = get(path, Long.class);
        return l == null ? 0 : l;
    }

    public static List<String> getStringList(String path) {
        checkConfig();
        if (listCache.containsKey(path)) {
            return listCache.get(path);
        }
        List<String> langList = languageConfig == null ? null : languageConfig.getStringList(path);
        List<String> list = langList == null ? config.getStringList(path) : langList;
        List<String> colored = new ArrayList<>();
        for (String line : list) {
            colored.add(translateColor(line));
        }
        listCache.put(path, colored);
        return getStringList(path);
    }

    public static List<Integer> getIntegerList(String path) {
        checkConfig();
        return config.getIntegerList(path);
    }

    @SuppressWarnings("unused")
    public static int[] getIntArray(String path) {
        List<Integer> list = getIntegerList(path);
        return list.stream().mapToInt(i -> i).toArray();
    }

    /** Loads mappings from input stream to cache. Input stream must be possible to read
     * as YAML file. Mappings are intended as an alternative method to support multiple
     * Minecraft versions, by replacing provided keys by provided values per-version.
     * This can be done by providing replacements for {@link org.bukkit.Material} and {@link Sound}, where these
     * enums differs from version to version.
     * <p>
     * There are three configuration sections that will be loaded into mappings when running this function:
     * <ul>
     *     <li>{@code general} - Always loaded.</li>
     *     <li>{@code 1_<version>} - Loads only when server version equals version</li>
     *     <li>{@code legacy} - Loads if server version is less than 1.13</li>
     * </ul>
     *
     * Example:
     *     <pre>
     * {@code general:
     *     DIAMOND_BLOCK: EMERALD_BLOCK
     * 1_8: #For servers running 1.8.x
     *    ENTITY_VILLAGER_TRADE: VILLAGER_YES
     * legacy: #For <1.13
     *    RED_TERRACOTTA: STAINED_CLAY:14
     *     }
     *     </pre>
     * */
    public static void loadMappings(InputStream stream) {
        if (stream == null) {
            return;
        }

        FileConfiguration mapping = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));

        ConfigurationSection generalSection = mapping.getConfigurationSection("general");
        if (generalSection != null) {
            for (String key : generalSection.getKeys(false)) {
                stringMappings.put(key, generalSection.getString(key));
            }
        }

        ConfigurationSection legacySection = mapping.getConfigurationSection("legacy");
        if (legacySection != null && VersionUtils.getMCVersion() < 13) {
            for (String key : legacySection.getKeys(false)) {
                stringMappings.put(key, legacySection.getString(key));
            }
        }
        ConfigurationSection versionSection = mapping.getConfigurationSection("1." + VersionUtils.getMCVersion());
        if (versionSection != null) {
            for (String key : versionSection.getKeys(false)) {
                stringMappings.put(key, versionSection.getString(key));
            }
        }
    }

    @SuppressWarnings("unused")
    public static ListBuilder getListBuilder(String path) {
        checkConfig();
        return new ListBuilder(getStringList(path));
    }

    public static ItemBuilder getItem(String path) {
        checkConfig();
        return new ItemBuilder(config.getConfigurationSection(path));
    }

    @SuppressWarnings("unused")
    public static CurrencyBuilder getCurrencyBuilder(String path) {
        return new CurrencyBuilder(getString(path));
    }

    /** Returns how much time until the given instant.
     * @param time Instant to calculate remaining time to.
     * @return Time remaining as a readable string. */
    @SuppressWarnings("unused")
    public static String getTimeLeft(Instant time) {
        if (time == null || time.getEpochSecond() == 0) {
            return getUnit("never", false);
        }
        Instant difference = time.minusSeconds(Instant.now().getEpochSecond()).truncatedTo(ChronoUnit.MINUTES);
        StringBuilder builder = new StringBuilder();
        int days = (int) Math.floor(difference.getEpochSecond() / 86400d);
        int hours = (int) Math.floor((difference.getEpochSecond() - (days * 86400d)) / 3600d);
        int minutes = (int) Math.ceil((difference.getEpochSecond() - (days * 86400d) - (hours * 3600d)) / 60);
        if (days > 0) {
            builder.append(days).append(" ").append(getUnit("d", days > 1)).append(" ");
        }
        if (hours > 0) {
            builder.append(hours).append(" ").append(getUnit("h", hours > 1)).append(" ");
        }
        if (minutes > 0) {
            builder.append(minutes).append(" ").append(getUnit("m", minutes > 1));
        }
        if (hours < 1 && minutes < 1 && days < 1)  {
            builder.append(getString("time.less_than_a_minute"));
        }
        return builder.toString();
    }

    /** Returns localized unit from configuration.
     * @param u Unit (s/m/h/d/never).
     * @param plural If localized unit should be in plural or not.
     * @return Localized unit. */
    public static String getUnit(String u, boolean plural) {
        if (u.equals("infinite")) {
            return getString("time.indefinitely");
        }
        switch (u) {
            case "s":
                return getString("time.second" + (plural ? "s" : ""));
            case "m":
                return getString("time.minute" + (plural ? "s" : ""));
            case "h":
                 return getString("time.hour" + (plural ? "s" : ""));
            case "d":
                return getString("time.day" + (plural ? "s" : ""));
            case "never":
                return getString("time.never");
            default:
                return "invalid unit";
        }
    }

    public static Object get(String path) {
        checkConfig();
        // Check cache
        if (cache.containsKey(path)) {
            return (cache.get(path));
        }
        Object confO = config.get(path);
        Object o = languageConfig != null && (confO == null || confO.equals(path)) ? languageConfig.get(path) : confO;
        cache.put(path, o);
        return null;
    }

    private static <T> T get(String path, Class<T> clazz) {
        checkConfig();
        // Check cache
        if (cache.containsKey(path) && clazz.isInstance(cache.get(path))) {
            return clazz.cast(cache.get(path));
        }
        Object confO = config.get(path);
        Object o = languageConfig != null && (!clazz.isInstance(confO) || confO.equals(path)) ? languageConfig.get(path) : confO;
        if (clazz.isInstance(o)) {
            cache.put(path, o);
            return clazz.cast(o);
        } else {
            return null;
        }
    }

    private static void checkConfig() {
        if (config == null) {
            throw new IllegalStateException("No FileConfiguration is loaded");
        }
    }

    /** Sets folder to load language files from.
     * @param languagesFolder Language folder. */
    public static void setLanguagesFolder(File languagesFolder) {
        ConfigManager.languagesFolder = languagesFolder;
    }

    /** Copies language files included in the plugin .jar to the set language folder.
     * @param plugin Plugin to load language files from.
     * @param languages Languages to load. */
    public static void loadLanguages(CorePlugin plugin, String... languages) {
        if (languagesFolder == null) {
            throw new IllegalStateException("No languages folder set");
        }
        for (String language : languages) {
            // Get version dependent language file
            InputStream stream = plugin.getResource(language + "_" + VersionUtils.getMCVersion() + ".yml");
            String fileName = language + "_" + VersionUtils.getMCVersion();

            if (stream == null && VersionUtils.getMCVersion() < 13) {
                stream = plugin.getResource(language + "_legacy" + ".yml");
                fileName = language + "_legacy";
            }

            fileName = stream == null ? language : fileName;

            File target = new File(languagesFolder, language + ".yml");
            try {
                if (!target.exists()) {
                    FileConfiguration targetConfig = YamlConfiguration.loadConfiguration(target);
                    targetConfig.save(target);
                }
                if (plugin.enableAutoUpdate()) {
                    ConfigUpdater.update(plugin, fileName + ".yml",  target);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        loadLanguageFile();
    }

    private static void loadLanguageFile() {
        String language = getString(languagePath);
        if (language == null || language.equals(languagePath)) {
            language = "en_US";
        }
        File languageFile = new File(languagesFolder, language + ".yml");
        if (languageFile.exists()) {
            languageConfig = YamlConfiguration.loadConfiguration(languageFile);
        }
    }

    /** Sets configuration path used to retrieve which language should be in use.
     * @param languagePath Path to language name. */
    @SuppressWarnings("unused")
    public static void setLanguagePath(String languagePath) {
        ConfigManager.languagePath = languagePath;
    }

    /** Adds color to provided string. Supports standard Minecraft colors using '&',
     * and hex colors using &#.
     * @param s String to colorize. */
    public static String translateColor(String s) {
        if (s == null) {
            return null;
        }
        if (VersionUtils.getMCVersion() < 16) {
            return ChatColor.translateAlternateColorCodes('&', s);
        }

        Matcher matcher = HEX_PATTERN.matcher(s);
        StringBuffer buffer = new StringBuffer();

        while(matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    /** Sets if spacing should be added between plugin prefix and
     * message for {@link #getMessage(String)}.
     * @param addPrefixSpace If space should be added or not. */
    @SuppressWarnings("unused")
    public static void setAddPrefixSpace(boolean addPrefixSpace) {
        ConfigManager.addPrefixSpace = addPrefixSpace;
    }

    public static boolean isCurrencyBefore() {
        return getBoolean(isBeforePath);
    }

    public static String getCurrency() {
        return getString(currencyPath);
    }

    public static String getPrefix() {
        return getString(prefixPath);
    }
}
