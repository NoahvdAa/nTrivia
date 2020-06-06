package me.noahvdaa.ntrivia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class nTrivia extends JavaPlugin {

	private static nTrivia instance;
	private boolean triviaActive = false;
	private String question = "";
	private List<String> answers = new ArrayList<String>();
	private long triviaStarted = 0;
	private long triviaUntil = 0;

	@Override
	public void onEnable() {
		// Save current instance.
		instance = this;

		saveDefaultConfig();

		// Warn owners if they don't have any questions.
		if (getConfig().getStringList("Questions").size() == 0) {
			getLogger().warning("WARNING: You don't have any questions set.");
		}

		// Warn owners if they don't have any reward commands.
		if (getConfig().getStringList("RewardCommands").size() == 0) {
			getLogger().warning("WARNING: You don't have any reward commands set.");
		}

		// Register events and commands.
		getServer().getPluginManager().registerEvents(new Events(), this);
		getCommand("ntrivia").setExecutor(new nTriviaCommand());

		// Schedule checker task.
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				if (triviaActive && triviaUntil < System.currentTimeMillis()) {
					// Time is up.
					Bukkit.broadcastMessage(getConfigMessage("Messages.notintime").replace("{question}", question)
							.replace("{answer}", answers.get(0)));
					endGame();
				} else if (!triviaActive && triviaStarted
						+ (getConfig().getInt("General.triviainterval") * 1000) < System.currentTimeMillis()) {
					// Its time to run trivia!

					// No players are online, so don't run.
					if (getServer().getOnlinePlayers().size() == 0)
						return;

					// Set time of last trivia question.
					triviaStarted = System.currentTimeMillis();

					// Are there any questions?
					if (getConfig().getStringList("Questions").size() != 0) {
						// Get the max time to answer.
						int sec = getConfig().getInt("General.answertime");

						// Get the list of questions.
						List<String> questions = getConfig().getStringList("Questions");
						// Get a random question.
						String[] questionsList = questions.get(new Random().nextInt(questions.size()))
								.split("\\|\\|\\|");
						if (questionsList.length == 1) {
							getLogger().warning("WARNING: Question doesn't have any answers. Skipping...");
							return;
						}
						// Mark trivia as active.
						triviaActive = true;

						// Get the question from the array.
						question = questionsList[0];
						// Remove the question, leaving only the answers
						questionsList = Arrays.stream(questionsList).skip(1).toArray(String[]::new);
						// Store the list of correct answers.
						answers = Arrays.asList(questionsList);
						// Store when the game should end.
						triviaUntil = System.currentTimeMillis() + (sec * 1000);
						// Announce game.
						Bukkit.broadcastMessage(getConfigMessage("Messages.announce").replace("{seconds}", sec + "")
								.replace("{question}", question));
					}
				}
			}
		}, 20, 20);

		// bStats metrics.
		Metrics metrics = new Metrics(this, 7762);

		if (!metrics.isEnabled()) {
			getLogger().info(
					"You have bStats metrics disabled. Consider enabling them, as it helps developers stay motivated. :)");
		}

		// Custom chart: amount of questions.
		metrics.addCustomChart(new Metrics.SingleLineChart("questions", new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getConfig().getStringList("Questions").size();
			}
		}));
	}

	/*
	 * Returns the current nTrivia instance.
	 */
	public static nTrivia getInstance() {
		if (instance == null) {
			instance = new nTrivia();
		}
		return instance;
	}

	/*
	 * Returns if a trivia question is active.
	 */
	public boolean getTriviaActive() {
		return this.triviaActive;
	}

	/*
	 * Returns the current active question. If trivia is not active, it returns an
	 * empty string.
	 */
	public String getCurrentQuestion() {
		return this.question;
	}

	/*
	 * Returns a StringList with correct answers for the active question. If trivia
	 * is not active, it returns an empty StringList.
	 */
	public List<String> getAnswersForQuestion() {
		return this.answers;
	}

	/*
	 * Returns when this trivia question started, or when the last trivia question
	 * was.
	 */
	public long getTriviaStartTime() {
		return this.triviaStarted;
	}

	/*
	 * Returns when this trivia question should end, or when the last trivia
	 * questoin ended.
	 */
	public long getTriviaUntil() {
		return this.triviaUntil;
	}

	/*
	 * Ends the game and resets values. Note: This will not broadcast a message.
	 */
	public void endGame() {
		this.triviaActive = false;
		this.question = "";
		this.answers = new ArrayList<String>();
		this.triviaStarted = System.currentTimeMillis();
	}

	/*
	 * Returns a chatcolor parsed string with the key specified or an error if
	 * nothing can be found.
	 */
	public String getConfigMessage(String message) {
		return ChatColor.translateAlternateColorCodes('&', getConfig().getString(message));
	}

	/*
	 * Forcefully starts the game by setting the last time a game happened to
	 * January the 1st, 1970.
	 */
	public void forceStartGame() {
		this.triviaStarted = 0;
	}

}
