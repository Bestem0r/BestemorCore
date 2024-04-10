package net.bestemor.core.config;

import net.bestemor.core.utils.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListBuilder {

    private final List<String> original;
    protected Map<String, String> replacements = new HashMap<>();
    protected Map<String, BigDecimal> currencyReplacements = new HashMap<>();

    protected ListBuilder(List<String> original) {
        this.original = original;
    }

    public ListBuilder replace(String sOld, String sNew) {
        replacements.put(sOld, sNew);
        return this;
    }

    @SuppressWarnings("unused")
    public ListBuilder replaceCurrency(String s, BigDecimal amount) {
        currencyReplacements.put(s, amount);
        return this;
    }

    public List<String> build() {

        List<String> result = new ArrayList<>();

        for (String line : original) {
            for (String sOld : replacements.keySet()) {
                line = line.replace(sOld, replacements.get(sOld));
            }
            if (!currencyReplacements.isEmpty()) {
                CurrencyBuilder b = new CurrencyBuilder(line);
                b.currencyReplacements = currencyReplacements;
                line = b.build();
            }
            result.add(Utils.parsePAPI(ConfigManager.translateColor(line)));
        }
        return result;
    }
}
