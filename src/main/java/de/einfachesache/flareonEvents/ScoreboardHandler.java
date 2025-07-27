package de.einfachesache.flareonEvents;

import de.einfachesache.flareonEvents.listener.PlayerDeathListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardHandler implements Listener {

    private static final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
    private static final Map<UUID, Scoreboard> playerBoards = new HashMap<>();

    public ScoreboardHandler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerSideboard(player);
                }
            }
        }.runTaskTimer(FlareonEvents.getPlugin(), 0L, 20L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        addScoreboardToPlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerBoards.remove(e.getPlayer().getUniqueId());
    }

    public static void addScoreboardToPlayer(Player player) {
        Scoreboard board = scoreboardManager.getNewScoreboard();
        Objective sidebar = board.registerNewObjective("FlareonEvents", Criteria.DUMMY, Component.text("§6§lFlareon Events"));

        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        playerBoards.put(player.getUniqueId(), board);
        player.setScoreboard(board);

        updatePlayerSideboard(player);
    }

    private static void updatePlayerSideboard(Player player) {
        Scoreboard board = playerBoards.get(player.getUniqueId());

        if (board == null) return;

        Objective sidebar = board.getObjective(DisplaySlot.SIDEBAR);

        if (sidebar == null) return;

        //Clear Entries
        board.getEntries().forEach(entry -> sidebar.getScore(entry).resetScore());

        setEmptyLines(sidebar, 11);

        // --- Zeit ---
        sidebar.getScore("§eZeit§7:").setScore(10);

        long startTime = Config.getStartTime() == 0 ? System.currentTimeMillis() : Config.getStartTime();
        long runningPause = (Config.getStopSince() != 0 ? System.currentTimeMillis() - Config.getStopSince() : 0);
        long secondsSinceStart = (System.currentTimeMillis() - startTime - runningPause) / 1000;
        long minutes = secondsSinceStart / 60;
        long seconds = secondsSinceStart % 60;

        String timeText = Config.getEventState() == EventState.RUNNING ? String.format("§f%d:%02d", minutes, seconds) : "0:00";
        sidebar.getScore(timeText).setScore(9);

        setEmptyLines(sidebar, 8);

        // --- Alive ---
        sidebar.getScore("§aPlayer Alive§7:").setScore(7);
        int aliveCount = (int) Bukkit.getOnlinePlayers().stream().filter(pp -> !pp.isDead() && !pp.isOp()).count();
        int total = Config.isEventStarted() ? Config.getParticipantsUUID().size() : aliveCount;
        sidebar.getScore("§f" + aliveCount + "§7/§f" + total).setScore(6);

        setEmptyLines(sidebar, 5);

        sidebar.getScore("§cKills§7:").setScore(4);
        int killCount = PlayerDeathListener.getPvpKillCounts().getOrDefault(player.getUniqueId(), 0);
        sidebar.getScore("§f" + killCount).setScore(3);

        setEmptyLines(sidebar, 2);

        // --- Border ---
        sidebar.getScore("§cBorder§7:").setScore(1);
        int size = (int) Math.round(player.getWorld().getWorldBorder().getSize());
        sidebar.getScore("§f" + size + "x" + size).setScore(0);

    }

    private static void setEmptyLines(Objective sidebar, int score) {
        sidebar.getScore(" ".repeat(score)).setScore(score);
    }
}