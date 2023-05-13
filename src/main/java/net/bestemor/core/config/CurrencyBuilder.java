package net.bestemor.core.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CurrencyBuilder {

    private String s;
    boolean addPrefix = false;
    protected final Map<String, String> replacements = new HashMap<>();
    protected Map<String, BigDecimal> currencyReplacements = new HashMap<>();

    protected CurrencyBuilder(String s) {
        this.s = s;
    }

    public String build() {
        boolean isBefore = ConfigManager.isCurrencyBefore();
        String currency = ConfigManager.getCurrency();

        for (String sOld : replacements.keySet()) {
            s = s.replace(sOld, replacements.get(sOld));
        }
        for (String sOld : currencyReplacements.keySet()) {
            BigDecimal formatted = new BigDecimal(currencyReplacements.get(sOld).toString())
                    .setScale(2, RoundingMode.HALF_UP)
                    .stripTrailingZeros();

            String amount = String.format(Locale.ENGLISH, "%,.2f", formatted);
            s = s.replace(sOld, isBefore ? (currency + amount) : (amount + currency));
        }
        if (addPrefix) {
            s = ConfigManager.getPrefix() + " " + s;
        }
        return s;
    }

    public CurrencyBuilder replace(String sOld, String sNew) {
        replacements.put(sOld, sNew);
        return this;
    }

    public CurrencyBuilder replaceCurrency(String replace, BigDecimal b) {
        currencyReplacements.put(replace, b);
        return this;
    }

    public CurrencyBuilder addPrefix() {
        addPrefix = true;
        return this;
    }
}
