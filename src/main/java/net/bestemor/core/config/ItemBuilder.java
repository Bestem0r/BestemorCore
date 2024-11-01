package net.bestemor.core.config;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.bestemor.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemBuilder {

    private final ConfigurationSection section;

    private final Map<String, BigDecimal> currencyReplacements = new HashMap<>();
    private final Map<String, String> replacements = new HashMap<>();

    private final Map<Enchantment, Integer> enchants = new HashMap<>();

    private boolean hideAttributes = true;

    public ItemBuilder(ConfigurationSection section) {
        this.section = section;
    }

    public ItemBuilder replace(String sOld, String sNew) {
        this.replacements.put(sOld, sNew);
        return this;
    }

    @SuppressWarnings("unused")
    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        this.enchants.put(enchantment, level);
        return this;
    }

    @SuppressWarnings("unused")
    public ItemBuilder replaceCurrency(String sOld, BigDecimal b) {
        this.currencyReplacements.put(sOld, b);
        return this;
    }

    @SuppressWarnings("unused")
    public ItemBuilder hideAttributes(boolean hideAttributes) {
        this.hideAttributes = hideAttributes;
        return this;
    }

    public ItemStack build() {
        String matString = getString("material");
        if (matString == null) {
            return new ItemStack(Material.STONE);
        }

        ItemStack item;
        if (matString.startsWith("hdb:") && Bukkit.getPluginManager().isPluginEnabled("HeadDatabase")) {
            HeadDatabaseAPI api = new HeadDatabaseAPI();
            item = api.getItemHead(matString.replace("hdb:", ""));
        } else if (matString.contains(":")) {
            String[] split = matString.split(":");
            item = new ItemStack(Material.valueOf(split[0]), 1, Short.parseShort(split[1]));
        } else {
            item = new ItemStack(Material.valueOf(matString));
        }
        int amount = section.getInt("amount");
        item.setAmount(amount > 0 ? amount : 1);

        String name = ConfigManager.translateColor(getString("name"));
        name = name == null || name.isEmpty() ? "" : Utils.parsePAPI(name);
        ListBuilder b = new ListBuilder(section.getStringList("lore"));
        int customModelData = section.getInt("model");
        b.currencyReplacements = currencyReplacements;
        b.replacements = replacements;

        List<String> tempLore = b.build();
        List<String> lore = new ArrayList<>();
        for (String s:tempLore) {
            lore.add(Utils.parsePAPI(s));
        }

        ItemMeta meta = item.getItemMeta();

        if (!currencyReplacements.isEmpty()) {
            CurrencyBuilder c = new CurrencyBuilder(name);
            c.currencyReplacements = currencyReplacements;
            name = c.build();
        }
        for (String sOld : replacements.keySet()) {
            name = name.replace(sOld, replacements.get(sOld));
        }

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            for (Enchantment enchantment : enchants.keySet()) {
                meta.addEnchant(enchantment, enchants.get(enchantment), true);
            }
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }
            if (hideAttributes) {
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    private String getString(String key) {
        return ConfigManager.getMappedString(section.getString(key));
    }
}
