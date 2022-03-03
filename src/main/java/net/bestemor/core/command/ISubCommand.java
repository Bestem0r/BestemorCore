package net.bestemor.core.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface ISubCommand {

    List<String> getCompletion(int index, String[] args);

    void run(CommandSender sender, String[] args);

    String getDescription();
    String getUsage();

    /** Returns true if subcommand requires special permission, false if not */
    boolean requirePermission();
}
