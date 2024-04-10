package net.bestemor.core.listener;

import net.bestemor.core.CorePlugin;
import net.bestemor.core.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static net.bestemor.core.utils.Utils.hasComma;
import static net.bestemor.core.utils.Utils.isNumeric;

/**
 * Listener for chat input
 * Used for getting string and decimal input from players
 */
public class ChatListener implements Listener {

    private final Map<UUID, Consumer<String>> stringListeners = new HashMap<>();
    private final Map<UUID, Consumer<BigDecimal>> decimalListeners = new HashMap<>();

    private String cancelInput;
    private final CorePlugin plugin;

    public ChatListener(CorePlugin plugin) {
        this.plugin = plugin;
        this.cancelInput = ConfigManager.getString("cancel");
    }

    /**
     * Updates the cancel input
     * @param cancelInput New cancel input
     */
    @SuppressWarnings("unused")
    public void setCancelInput(String cancelInput) {
        this.cancelInput = cancelInput;
    }

    /**
     * Adds a listener for string input
     * @param player Player to listen to
     * @param result Consumer to accept the input
     */
    @SuppressWarnings("unused")
    public void addStringListener(Player player, Consumer<String> result) {
        player.sendMessage(ConfigManager.getMessage("messages.type_cancel").replace("%cancel%", cancelInput));
        stringListeners.put(player.getUniqueId(), result);
    }

    /**
     * Adds a listener for decimal input
     * @param player Player to listen to
     * @param result Consumer to accept the input
     */
    @SuppressWarnings("unused")
    public void addDecimalListener(Player player, Consumer<BigDecimal> result) {
        player.sendMessage(ConfigManager.getMessage("messages.type_cancel").replace("%cancel%", cancelInput));
        decimalListeners.put(player.getUniqueId(), result);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {

        UUID uuid = event.getPlayer().getUniqueId();

        if (stringListeners.containsKey(uuid) || decimalListeners.containsKey(uuid)) {
            event.setCancelled(true);

            String message = ChatColor.stripColor(event.getMessage()).replace("§", "").replace("&", "");
            Player player = event.getPlayer();

            if (message.equalsIgnoreCase(cancelInput)) {
                player.sendMessage(ConfigManager.getMessage("messages.cancelled"));
                decimalListeners.remove(uuid);
                stringListeners.remove(uuid);

            } else if (stringListeners.containsKey(uuid)) {
                Consumer<String> consumer = stringListeners.get(uuid);
                stringListeners.remove(uuid);
                Bukkit.getScheduler().runTask(plugin, () -> consumer.accept(event.getMessage()));

            } else if (decimalListeners.containsKey(uuid)) {
                if (!isNumeric(message)) {
                    player.sendMessage(hasComma(message) ? ConfigManager.getMessage("messages.use_dot") : ConfigManager.getMessage("messages.not_number"));
                } else if (Double.parseDouble(message) < 0) {
                    player.sendMessage(ConfigManager.getMessage("messages.negative_price"));
                } else {
                    Consumer<BigDecimal> consumer = decimalListeners.get(uuid);
                    decimalListeners.remove(uuid);
                    Bukkit.getScheduler().runTask(plugin, () -> consumer.accept(new BigDecimal(message)));
                }
            }

        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        stringListeners.remove(event.getPlayer().getUniqueId());
    }
}
