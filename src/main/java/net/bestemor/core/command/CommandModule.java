package net.bestemor.core.command;

import net.bestemor.core.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class CommandModule implements CommandExecutor, TabCompleter {

    private Map<String, ISubCommand> subCommands;
    private JavaPlugin plugin;

    private String permissionPrefix;
    private ChatColor pluginNameChatColor = ChatColor.AQUA;

    private CommandModule() {}

    public static class Builder {

        private final JavaPlugin plugin;
        private final Map<String, ISubCommand> subCommands = new HashMap<>();
        private String permissionPrefix = "command";

        public Builder(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        public Builder addSubCommand(String label, ISubCommand ISubCommand) {
            subCommands.put(label, ISubCommand);
            return this;
        }

        public Builder permissionPrefix(String prefix) {
            this.permissionPrefix = prefix;
            return this;
        }

        public CommandModule build() {
            CommandModule commandModule = new CommandModule();
            commandModule.subCommands = subCommands;
            commandModule.plugin = plugin;
            commandModule.permissionPrefix = permissionPrefix;
            return commandModule;
        }
    }

    private class HelpCommandI implements ISubCommand {

        private final String mainCommandName;

        private HelpCommandI(String mainCommandName) {
            this.mainCommandName = mainCommandName;
        }

        @Override
        public List<String> getCompletion(int index, String[] args) {
            return new ArrayList<>(); }

        @Override
        public void run(CommandSender sender, String[] args) {
            List<String> help = new ArrayList<>();
            help.add("§l§m------§r " + pluginNameChatColor + "§l" + plugin.getName() + " Commands §r§l§m------");
            subCommands.forEach((k, v) -> {
                help.add("§b/" + mainCommandName + " " + k + " " + v.getUsage() + "§7 - " + v.getDescription());
            });
            help.forEach(sender::sendMessage);
        }
        @Override
        public String getDescription() {
            return "Show commands";
        }

        @Override
        public String getUsage() {
            return "";
        }

        @Override
        public boolean requirePermission() {
            return true;
        }
    }

    /** Registers CommandModule as command
     * @param command Command name */
    public void register(String command) {
        HelpCommandI helpCommand = new HelpCommandI(command);
        subCommands.put("help", helpCommand);

        Objects.requireNonNull(plugin.getCommand(command)).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand(command)).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender,Command command, String s, String[] args) {
        if (args.length == 0 || !subCommands.containsKey(args[0])) {
            sender.sendMessage(ConfigManager.getMessage("messages.invalid_command_usage"));
            return true;
        }

        if (subCommands.get(args[0]).requirePermission() && sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission(permissionPrefix + "." + args[0])) {
                player.sendMessage(ConfigManager.getMessage("messages.no_permission_command"));
                return true;
            }
        }
        subCommands.get(args[0]).run(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

        if (args.length == 0 || args[0].length() == 0) {
            return new ArrayList<>(subCommands.keySet());
        }

        //Tab completion for registered sub-commands
        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(c -> c.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        if (subCommands.containsKey(args[0])) {
            return subCommands.get(args[0]).getCompletion(args.length - 2, args);
        }
        return null;
    }
}
