package de.einfachesache.flareonevents.handler;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.event.EventState;
import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.listener.PlayerDeathListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardHandler implements Listener {

    private static final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
    private static final Map<UUID, Scoreboard> playerBoards = new HashMap<>();
    private static final Map<World, Integer> cachedWorldBorders = new HashMap<>();
    private static final String[] EMPTY_LINES = {"§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9"};

    private static volatile ScoreboardContext cachedContext;

    public record ScoreboardContext(
            long now,
            int alive,
            int total,
            Component tablistHeader,
            Component tablistFooter
    ) {
    }

    public ScoreboardHandler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                update();
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

        updatePlayer(player);
    }

    private static void update() {
        long now = System.currentTimeMillis();
        int aliveCount = (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.isDead() && !p.isOp()).count();
        int totalCount = Config.isEventStarted()
                ? Config.getParticipantsUUID().size()
                : aliveCount;

        int teams = Config.getTeams().size();
        Component eventPhase = switch (Config.getEventState()) {
            case NOT_RUNNING -> Component.text(EventState.NOT_RUNNING.getName(), NamedTextColor.RED);
            case PREPARING -> Component.text(EventState.PREPARING.getName(), NamedTextColor.YELLOW);
            case STARTING -> Component.text(EventState.STARTING.getName(), NamedTextColor.GOLD);
            case RUNNING -> Component.text(EventState.RUNNING.getName(), NamedTextColor.GREEN);
            case ENDED -> Component.text(EventState.ENDED.getName(), NamedTextColor.RED);
        };

        Component header = Component.text()
                .append(Component.text("Flareon Events", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Teams: ", NamedTextColor.GRAY))
                .append(Component.text(String.valueOf(teams), NamedTextColor.WHITE))
                .build();

        Component footer = Component.text()
                .append(Component.newline())
                .append(Component.text("Phase: ", NamedTextColor.GRAY))
                .append(eventPhase)
                .append(Component.newline())
                .append(Component.text("Discord: ", NamedTextColor.GRAY))
                .append(Component.text("flareonevents.de/discord", NamedTextColor.BLUE))
                .build();

        cachedContext = new ScoreboardContext(now, aliveCount, totalCount, header, footer);

        cachedWorldBorders.clear();
        Bukkit.getWorlds().forEach(world ->
                cachedWorldBorders.put(world, (int) Math.round(world.getWorldBorder().getSize()))
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    private static void updatePlayer(Player player) {

        if (cachedContext == null) {
            cachedContext = new ScoreboardContext(System.currentTimeMillis(), 0, 0, Component.empty(), Component.empty());
        }

        updateTablist(player);
        updatePlayerSideboard(player);
    }

    private static void updateTablist(Player player) {
        int teamID = Config.getPlayerTeams().getOrDefault(player.getUniqueId(), -1);
        String prefix, suffix = teamID == -1 ? "" : "§7 [" + teamID + "]";
        int listOrder;

        if (FlareonEvents.DEV_UUID.equals(player.getUniqueId())) {
            prefix = "§4DEV | ";
            listOrder = Integer.MAX_VALUE;
        } else if (Arrays.stream(FlareonEvents.ORGA_UUID).anyMatch(orgaUUID -> orgaUUID.equals(player.getUniqueId()))) {
            prefix = "§4ORGA | ";
            listOrder = Integer.MAX_VALUE - 1;
        } else if (player.isOp()) {
            prefix = "§cSTAFF | ";
            listOrder = Integer.MAX_VALUE - 2;
        } else {
            prefix = "§a";
            listOrder = (teamID != -1) ? 1000 - teamID : 1000;
        }

        Component display = Component.text(prefix + player.getName() + suffix);

        player.setPlayerListOrder(listOrder);
        player.playerListName(display);
        player.displayName(display);
        player.customName(display);
        player.setCustomNameVisible(true);
        player.sendPlayerListHeaderAndFooter(cachedContext.tablistHeader, cachedContext.tablistFooter);
    }

    private static void updatePlayerSideboard(Player player) {
        UUID uuid = player.getUniqueId();
        Scoreboard board = playerBoards.get(uuid);
        if (board == null) return;

        Objective sidebar = board.getObjective(DisplaySlot.SIDEBAR);
        if (sidebar == null) return;

        board.getEntries().forEach(board::resetScores);

        int score = 11;

        setEmptyLine(sidebar, score--);

        // Time
        sidebar.getScore("§eZeit§7:").setScore(score--);

        long now = cachedContext.now;
        long startTime = Config.getStartTime() == 0 ? now : Config.getStartTime();
        long pauseDuration = Config.getStopSince() != 0 ? now - Config.getStopSince() : 0;
        long seconds = (now - startTime - pauseDuration) / 1000;

        String timeText = Config.getEventState() == EventState.RUNNING
                ? String.format("§f%d:%02d", seconds / 60, seconds % 60)
                : "0:00";
        sidebar.getScore(timeText).setScore(score--);

        setEmptyLine(sidebar, score--);

        // Alive
        sidebar.getScore("§aÜberlebende§7:").setScore(score--);
        int alive = cachedContext.alive();
        int total = cachedContext.total();
        sidebar.getScore("§f" + alive + "§7/§f" + total).setScore(score--);

        setEmptyLine(sidebar, score--);

        // Kills
        sidebar.getScore("§cKills§7:").setScore(score--);
        int killCount = PlayerDeathListener.getPvpKillCounts().getOrDefault(uuid, 0);
        sidebar.getScore("§f" + killCount).setScore(score--);

        setEmptyLine(sidebar, score--);

        // Border
        sidebar.getScore("§cBorder§7:").setScore(score--);
        int size = cachedWorldBorders.getOrDefault(player.getWorld(), 0);
        sidebar.getScore("§f" + size + "x" + size).setScore(score);

    }

    private static void setEmptyLine(Objective sidebar, int score) {
        sidebar.getScore(EMPTY_LINES[score % EMPTY_LINES.length]).setScore(score);
    }
}