package net.bestemor.core.config;

import net.bestemor.core.utils.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemBuilder {

    private final String path;

    private final Map<String, BigDecimal> currencyReplacements = new HashMap<>();
    private final Map<String, String> replacements = new HashMap<>();

    private final Map<Enchantment, Integer> enchants = new HashMap<>();

    protected ItemBuilder(String path) {
        this.path = path;
    }

    public ItemBuilder replace(String sOld, String sNew) {
        replacements.put(sOld, sNew);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        enchants.put(enchantment, level);
        return this;
    }

    public ItemBuilder replaceCurrency(String sOld, BigDecimal b) {
        currencyReplacements.put(sOld, b);
        return this;
    }

    public ItemStack build() {
        String matString = ConfigManager.getString(path + ".material");

        ItemStack item;
        if (matString.contains(":")) {
            String[] split = matString.split(":");
            item = new ItemStack(Material.valueOf(split[0]), 1, Short.parseShort(split[1]));
        } else {
            item = new ItemStack(Material.valueOf(ConfigManager.getString(path + ".material")));
        }
        String name = ConfigManager.getString(path + ".name");
        name = name == null || name.equals("") || name.equals(path + ".name") ? "" : Utils.parsePAPI(name);
        ListBuilder b = new ListBuilder(path + ".lore");
        int customModelData = ConfigManager.getInt(path + ".model");
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
        }

        item.setItemMeta(meta);
        return item;
    }
}
