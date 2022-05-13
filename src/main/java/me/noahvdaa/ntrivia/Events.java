package me.noahvdaa.ntrivia;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class Events implements Listener {

	@EventHandler
	public void playerJoin(PlayerJoinEvent e) {
		// Only continue if trivia is active.
		if (!nTrivia.getInstance().getTriviaActive())
			return;
		// How long will trivia run for?
		long triviaWillBeFor = nTrivia.getInstance().getTriviaUntil() - System.currentTimeMillis();
		// Convert to seconds.
		double seconds = Math.ceil(triviaWillBeFor / 1000);
		// Tell them 1 second later to prevent the message getting caught up in other on
		// join messages.
		nTrivia.getInstance().getServer().getScheduler().runTaskLater(nTrivia.getInstance(), new Runnable() {
			@Override
			public void run() {
				// If the player already left, or if the game isn't active anymore, just ignore.
				if (!e.getPlayer().isOnline() || !nTrivia.getInstance().getTriviaActive())
					return;
				// Re-broadcast the message, replacing the double's .0 with nothing. (18.0 ->
				// 18)
				e.getPlayer()
						.sendMessage(nTrivia.getInstance().getConfigMessage("Messages.announce")
								.replace("{seconds}", Double.toString(seconds).replace(".0", ""))
								.replace("{question}", nTrivia.getInstance().getCurrentQuestion()));
			}
		}, 20);
	}

	@EventHandler
	public void asyncPlayerChat(AsyncPlayerChatEvent e) {
		// Only continue if trivia is active.
		if (!nTrivia.getInstance().getTriviaActive())
			return;
		// Ignore commands.
		if (e.getMessage().startsWith("/"))
			return;
		// Store if we found a match and if so, which.
		boolean foundAnswer = false;
		String match = "";
		for (String a : nTrivia.getInstance().getAnswersForQuestion()) {
			if (e.getMessage().toLowerCase().contains(a.toLowerCase())) {
				foundAnswer = true;
				match = a;
				break;
			}
		}
		// We didn't find the answer, skip.
		if (!foundAnswer)
			return;
		final String matched = match;

		// Cancel the message event.
		e.setCancelled(true);

		// Ms taken. Also shitty number conversion.
		int ms = Integer
				.parseInt(Double.toString(System.currentTimeMillis() - nTrivia.getInstance().getTriviaStartTime()).replace(".0", ""));
		double sec = Math.floor(ms / 1000);
		double msremaining = ms - (sec * 1000);
		String timetaken = Double.toString(sec).replace(".0", "") + "." + Double.toString(msremaining).replace(".0", "");

		nTrivia.getInstance().getServer().getScheduler().runTask(nTrivia.getInstance(), () -> {
			// Announce the fact that someone won.
			Bukkit.broadcastMessage(
					nTrivia.getInstance().getConfigMessage("Messages.won").replace("{player}", e.getPlayer().getName())
							.replace("{question}", nTrivia.getInstance().getCurrentQuestion()).replace("{answer}", matched)
							.replace("{timetaken}", timetaken));
			// End the game.
			nTrivia.getInstance().endGame();
			// Run commands.
			String name = e.getPlayer().getName();
			for (String cmd : nTrivia.getInstance().getConfig().getStringList("RewardCommands")) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", name));
			}
		});
	}

}
