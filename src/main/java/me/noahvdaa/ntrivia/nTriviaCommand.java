package me.noahvdaa.ntrivia;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class nTriviaCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// If no sub command is provided or the user doesn't have permissions, show the
		// plugin version.
		if (args.length == 0 || !sender.hasPermission("ntrivia.admin")) {
			sender.sendMessage(ChatColor.YELLOW + "nTrivia " + ChatColor.AQUA + "v"
					+ nTrivia.getInstance().getDescription().getVersion() + " " + ChatColor.YELLOW + "by NoahvdAa");
			return true;
		}
		// Switch by subcommand.
		switch (args[0].toLowerCase()) {
		case "forcestart":
			// If a trivia question is already active, we can't start another.
			if (nTrivia.getInstance().getTriviaActive()) {
				sender.sendMessage(ChatColor.RED + "A question is already active!");
				break;
			}
			// Force start a question.
			nTrivia.getInstance().forceStartGame();
			sender.sendMessage(ChatColor.GREEN + "Force-started question!");
			if(nTrivia.getInstance().getServer().getOnlinePlayers().size() < nTrivia.getInstance().getConfig().getInt("General.minplayers")) {
				sender.sendMessage(ChatColor.YELLOW + "There are not enough players online to start a trivia question (minplayers in the configuration file). The question will be asked as soon as there are enough players.");
			}
			break;
		case "reload":
			sender.sendMessage(ChatColor.GOLD + "Reloading...");

			nTrivia.getInstance().reloadConfig();
			// Yell at them if they forgot anything.
			if (nTrivia.getInstance().getConfig().getStringList("Questions").size() == 0) {
				sender.sendMessage(ChatColor.RED + "WARNING: You don't have any questions set.");
			}

			if (nTrivia.getInstance().getConfig().getStringList("RewardCommands").size() == 0) {
				sender.sendMessage(ChatColor.RED + "WARNING: You don't have any reward commands set.");
			}

			sender.sendMessage(ChatColor.GREEN + "Reloaded!");
			break;
		default:
			// Fallback command.
			sender.sendMessage(ChatColor.RED + "Invalid subcommand. Valid subcommands are: forcestart, reload.");

		}

		return true;
	}

}
