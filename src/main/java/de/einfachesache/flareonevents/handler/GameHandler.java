package de.einfachesache.flareonevents.handler;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.event.EventSound;
import de.einfachesache.flareonevents.event.EventState;
import de.einfachesache.flareonevents.item.misc.EventInfoBook;
import de.einfachesache.flareonevents.listener.PlayerDeathListener;
import de.einfachesache.flareonevents.listener.PortalCreateListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class GameHandler {

    static final FlareonEvents plugin = FlareonEvents.getPlugin();
    static final List<BukkitTask> tasks = new ArrayList<>();
    static final Map<UUID, Location> playerAssignedSpawns = new HashMap<>();

    static final int preparingTime = 3 * 60; // DEFAULT: 3 * 60
    static final int startingTime = 2 * 60; // DEFAULT: 2 * 60
    static final int noPvPTime = 5 * 60; // DEFAULT: 5 * 60
    static final int netherOpenTime = 60 * 60; // DEFAULT: 60 * 60

    static String winner = "§cNO WINNER§6";

    static String secondWord(int n) {
        return n <= 1 ? "Sekunde" : "Sekunden";
    }

    static String minuteWord(int n) {
        return n <= 1 ? "Minute" : "Minuten";
    }

    static final Title.Times times = Title.Times.times(
            Duration.ofMillis(1000),  // 20 Ticks = 1 Sekunde (fadeIn)
            Duration.ofMillis(3000),  // 60 Ticks = 3 Sekunden (stay)
            Duration.ofMillis(1000)   // 20 Ticks = 1 Sekunde (fadeOut)
    );

    public static void prepareEvent(boolean forceStart) {

        tasks.forEach(BukkitTask::cancel);

        Config.setEventState(EventState.PREPARING);
        Config.deleteAllTeams();
        Config.clearParticipant();
        Config.clearDeathParticipant();
        Config.setStopSince(0);

        PlayerDeathListener.getPvpKillCounts().clear();
        PortalCreateListener.setNether(false);

        playerAssignedSpawns.clear();

        plugin.getServer().getWorlds().forEach(world -> world.setPVP(false));

        World world = Bukkit.getWorlds().getFirst();
        world.setTime(6000);
        world.setClearWeatherDuration(20 * 60 * 20);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);

        int minStartTime = (forceStart ? 1 / 12:  (preparingTime + startingTime) / 60);
        Bukkit.getServer().getOnlinePlayers().forEach(player -> {

            player.showTitle(Title.title(
                    Component.text("BEREIT?", NamedTextColor.GREEN),
                    Component.text("Das Event startet in " + minStartTime + " " + minuteWord(minStartTime) + "!", NamedTextColor.YELLOW),
                    times));
            player.playSound(EventSound.NOTIFY.adventure());

            if(player.getGameMode() == GameMode.SPECTATOR) {
                SpectatorHandler.stopSpectating(player);
            }

            resetPlayer(player, true, false);
        });

        eventStartScheduler(forceStart);
        startCountdownTimerSchedule(forceStart);
    }

    private static void eventStartScheduler(boolean forceStart) {

        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {

                Config.setEventState(EventState.STARTING);

                Iterator<Location> playerSpawnLocationIterator = Config.getPlayerSpawnLocations().stream().iterator();

                Bukkit.getServer().getOnlinePlayers().stream()
                        .filter(player -> !player.isOp())
                        .forEach(player -> {
                            if (playerSpawnLocationIterator.hasNext()) {

                                Config.addParticipant(player.getUniqueId());

                                Location playerSpawnLocation = playerSpawnLocationIterator.next().toHighestLocation().add(0, 1, 0);
                                player.teleportAsync(playerSpawnLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                                playerAssignedSpawns.put(player.getUniqueId(), playerSpawnLocation);

                                Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0);
                                player.setGameMode(GameMode.ADVENTURE);
                                player.clearActivePotionEffects();
                                player.setVelocity(new Vector());
                                player.setWalkSpeed(0.0f);
                                player.setFlying(false);

                                player.showTitle(Title.title(
                                        Component.text("ACHTUNG!", NamedTextColor.GREEN),
                                        Component.text("Event Start in Kürze!", NamedTextColor.YELLOW),
                                        times));
                                player.playSound(EventSound.NOTIFY.adventure());

                            } else {
                                player.kick(Component.text("§4❌ Kein Spawnpunkt mehr verfügbar. \n ❌ Du bist leider nicht dabei"));
                            }
                        });

                plugin.getServer().broadcast(Component.text("Mach dich bereit, der Countdown läuft. Das Event startet in Kürze!", NamedTextColor.YELLOW));

                tasks.add(new BukkitRunnable() {
                    @Override
                    public void run() {

                        Config.setEventState(EventState.RUNNING);
                        Config.setStartTime(System.currentTimeMillis());

                        startPvPSchedule();
                        startNetherOpenSchedule();
                        startBorderSchedule();

                        Bukkit.getServer().getOnlinePlayers().forEach(
                                player -> resetPlayer(player, true, true));
                    }
                }.runTaskLater(plugin,  (forceStart ? 5 : startingTime) * 20L));

            }
        }.runTaskLater(plugin,  (forceStart ? 5 : preparingTime) * 20L));
    }

    public static void cancelEvent() {

        tasks.forEach(BukkitTask::cancel);

        Config.setEventState(EventState.NOT_RUNNING);
        Config.setStopSince(System.currentTimeMillis());

        PortalCreateListener.setNether(false);

        plugin.getServer().getWorlds().forEach(world -> world.setPVP(false));
        plugin.getServer().getOnlinePlayers().forEach(player -> {

            if(player.getGameMode() == GameMode.SPECTATOR) {
                SpectatorHandler.stopSpectating(player);
            }

            resetPlayer(player, true, true);

            player.showTitle(Title.title(
                    Component.text("STOP!", NamedTextColor.RED),
                    Component.text("Das Event wurde gestoppt!", NamedTextColor.YELLOW), times));
            player.playSound(EventSound.NOTIFY.adventure());
            player.teleportAsync(Config.getMainSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.getInventory().setItem(8, EventInfoBook.createEventInfoBook());

        });
        plugin.getServer().broadcast(Component.text("Das Event wurde kurzzeitig gestoppt. Das Event startet in Kürze erneut!", NamedTextColor.RED));
    }

    public static void pauseEvent() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Config.setEventState(EventState.NOT_RUNNING);
            Config.setStopSince(System.currentTimeMillis());

            plugin.getServer().getWorlds().forEach(world -> world.setPVP(false));
            plugin.getServer().getOnlinePlayers().forEach(player -> {

                player.showTitle(Title.title(
                        Component.text("Pause!", NamedTextColor.YELLOW),
                        Component.text("Das Event wurde pausiert!", NamedTextColor.YELLOW), times));
                player.playSound(EventSound.NOTIFY.adventure());

                if (!player.isOp()) {
                    Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0);
                    player.setVelocity(new Vector());
                    player.setWalkSpeed(0.0f);
                    player.setFlying(false);
                }
            });
            plugin.getServer().broadcast(Component.text("Das Event wurde kurzzeitig pausiert. Das Event startet in Kürze erneut!", NamedTextColor.YELLOW));
        }, 5);
    }

    public static void endEvent() {

        tasks.forEach(BukkitTask::cancel);

        Config.setEventState(EventState.ENDED);
        Config.setStopSince(System.currentTimeMillis());

        plugin.getServer().getWorlds().forEach(world -> world.setPVP(false));
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            player.showTitle(Title.title(
                    Component.text("Event Beendet!", NamedTextColor.RED),
                    Component.text( winner + " hat gewonnen!", NamedTextColor.GOLD), times));
            player.playSound(EventSound.START_END.adventure());
        });
        plugin.getServer().broadcast(Component.text("Das Event ist beendet. Vielen Dank für eure Teilnahme!", NamedTextColor.GOLD));
    }

    public static boolean isEventEnd() {
        int aliveTeams = Config.getTeams().size();
        if (aliveTeams > 1) return false;

        List<Player> aliveSoloPlayer = Bukkit.getOnlinePlayers().stream()
                .filter(player -> !player.isOp())
                .filter(player -> player.getGameMode() == GameMode.SURVIVAL)
                .filter(player -> !Config.getDeathParticipantsUUID().contains(player.getUniqueId().toString()))
                .filter(player -> !Config.getPlayerTeams().containsKey(player.getUniqueId())).collect(Collectors.toUnmodifiableList());

        if (aliveTeams + aliveSoloPlayer.size() > 1) {
            return false;
        }

        if (aliveTeams == 1) {
            winner = "§cTeam " + Config.getTeams().entrySet().iterator().next().getKey() + "§6";
        } else if (!aliveSoloPlayer.isEmpty()) {
            winner = "§c" + aliveSoloPlayer.getFirst().getName() + "§6";
        }

        return true;
    }


    private static void startCountdownTimerSchedule(boolean forceStart) {

        tasks.add(new BukkitRunnable() {
            int secondsLeft = (forceStart ? 10 : (preparingTime + startingTime));

            @Override
            public void run() {

                if (secondsLeft <= 0) {

                    int noPvPTimeMin = noPvPTime / 60;
                    int netherOpenTimeMin = netherOpenTime / 60;

                    plugin.getServer().getOnlinePlayers().forEach(player -> {
                        player.sendActionBar(Component.text("Event ist gestartet", NamedTextColor.DARK_RED));
                        player.showTitle(Title.title(
                                Component.text("ACHTUNG", NamedTextColor.RED),
                                Component.text("Event ist gestartet", NamedTextColor.YELLOW),
                                times));

                        player.sendMessage(Component.text("""
                                §6[Event] §eDas Event startet jetzt!
                                §aViel Spaß und viel Erfolg!
                                §eMax Teamgröße ist §c%d§e Spieler!
                                §ePvP beginnt in §c%d %s§e!
                                §5Der Nether öffnet in §c%d %s§5!"""
                                .formatted(Config.getMaxTeamSize(), noPvPTimeMin, minuteWord(noPvPTimeMin), netherOpenTimeMin, minuteWord(netherOpenTimeMin))
                        ));

                        player.playSound(EventSound.START_END.adventure());
                    });

                    this.cancel();
                    return;
                }

                int minutes = secondsLeft / 60;
                int seconds = secondsLeft % 60;
                String timeFormatted = String.format("§eStart in §c%d:%02d §e%s", minutes, seconds, minuteWord(minutes));

                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    if (secondsLeft == 120 || secondsLeft == 60 || secondsLeft == 30 || secondsLeft <= 10) {
                        player.showTitle(Title.title(
                                Component.text(secondsLeft <= 60 ? String.format("§eNoch §a%d §e%s", secondsLeft, secondWord(secondsLeft)) : String.format("§eNoch §a%d §e%s", minutes, minuteWord(minutes)), NamedTextColor.RED),
                                Component.text(secondsLeft == 120 ? "Mache dich bereit!" : "", NamedTextColor.YELLOW),
                                times));
                        player.playSound(EventSound.NOTIFY.adventure());
                    }

                    player.sendActionBar(Component.text(timeFormatted));
                });

                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L));
    }

    private static void startPvPSchedule() {

        tasks.add(new BukkitRunnable() {
            int secondsLeft = noPvPTime;

            @Override
            public void run() {

                if (secondsLeft <= 0) {

                    plugin.getServer().getWorlds().forEach(world -> world.setPVP(true));

                    plugin.getServer().getOnlinePlayers().forEach(player -> {
                        player.sendActionBar(Component.text("PVP ist ab jetzt möglich", NamedTextColor.DARK_RED));
                        player.showTitle(Title.title(
                                Component.text("ACHTUNG", NamedTextColor.RED),
                                Component.text("PVP ist ab jetzt möglich", NamedTextColor.YELLOW),
                                times));
                        player.playSound(EventSound.NOTIFY.adventure());
                    });

                    this.cancel();
                    return;
                }

                int minutes = secondsLeft / 60;
                int seconds = secondsLeft % 60;
                String timeFormatted = String.format("§ePVP in §c%d:%02d §e%s", minutes, seconds, minuteWord(minutes));

                plugin.getServer().getOnlinePlayers().forEach(player -> player.sendActionBar(Component.text(timeFormatted)));

                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L));
    }

    private static void startNetherOpenSchedule() {

        tasks.add(new BukkitRunnable() {
            int secondsLeft = netherOpenTime - noPvPTime;

            @Override
            public void run() {

                if (secondsLeft <= 0) {

                    PortalCreateListener.setNether(true);

                    plugin.getServer().getOnlinePlayers().forEach(player -> {
                        player.sendActionBar(Component.text("Nether ist nun geöffnet", NamedTextColor.DARK_RED));
                        player.playSound(EventSound.NOTIFY.adventure());
                    });

                    this.cancel();
                    return;
                }

                int minutes = secondsLeft / 60;
                int seconds = secondsLeft % 60;
                String timeFormatted = String.format("§eNether öffnet in §c%d:%02d §e%s", minutes, seconds, minuteWord(minutes));

                plugin.getServer().getOnlinePlayers().forEach(player -> player.sendActionBar(Component.text(timeFormatted)));

                secondsLeft--;
            }
        }.runTaskTimer(plugin, noPvPTime * 20L, 20L));
    }


    private static void startBorderSchedule() {

        List<World> worlds = Bukkit.getWorlds();

        // Nach 40 Minuten (40 * 60 * 20 Ticks)
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                // Verkleinere in 20 Minuten (20 * 60 Sekunden) auf 2000
                moveBorder(worlds, 2000, 20);
            }
        }.runTaskLater(plugin, 40 * 60 * 20L));

        // Nach 90 Minuten (30 min + 20 min + 40 min)
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                // Verkleinere in 30 Minuten auf 1500
                moveBorder(worlds, 1500, 30);
            }
        }.runTaskLater(plugin, 90 * 60 * 20L));

        // Nach 140 Minuten (20min + 30min + 30 min + 20 min + 40 min)
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                // Verkleinere in 40 Minuten auf 200
                moveBorder(worlds, 200, 40);
            }
        }.runTaskLater(plugin, 140 * 60 * 20L));

        // Nach 210 Minuten (30min + 40min + 20min + 30min + 30 min + 20 min + 40 min)
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                // Verkleinere in 10 Minuten auf 50
                moveBorder(worlds, 50, 10);
            }
        }.runTaskLater(plugin, 210 * 60 * 20L));

        // Nach 240 Minuten (20min + 10min + 30min + 40min + 20min + 30min + 30 min + 20 min + 40 min)
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                // Verkleinere in 5 Minuten auf 5
                moveBorder(worlds, 5, 5);
            }
        }.runTaskLater(plugin, 240 * 60 * 20L));

        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getServer().broadcast(Component.text("Die Border ist stehen geblieben. Das Gebiet bleibt nun auf 5x5 Blöcke begrenzt!", NamedTextColor.YELLOW));
            }
        }.runTaskLater(plugin, 245 * 60 * 20L));
    }

    private static void moveBorder(List<World> worlds, int size, int delayInMinutes) {
        Title title = Title.title(
                Component.text("ACHTUNG", NamedTextColor.RED),
                Component.text("Die Border bewegt sich nun zu " + size + "x" + size, NamedTextColor.YELLOW),
                times);

        plugin.getServer().getOnlinePlayers().forEach(player -> {
            player.showTitle(title);
            player.playSound(EventSound.NOTIFY.adventure());
            player.sendMessage(Component.text("Pass auf! Die Border hat sich in Bewegung gesetzt. Das Gebiet wird nun auf " + size + "x" + size + " Blöcke begrenzt!", NamedTextColor.YELLOW));
        });

        worlds.forEach(world -> world.getWorldBorder().setSize(size, TimeUnit.MINUTES, delayInMinutes));
    }

    public static void resetPlayer(Player player, boolean potionReset, boolean completeReset) {
        if (potionReset) {
            player.lockFreezeTicks(false);
            player.clearActivePotionEffects();
        }

        if (completeReset || Config.getEventState() != EventState.STARTING) {
            Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0.42);
            player.setWalkSpeed(0.2f);
        }

        if (completeReset) {
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(10);
            player.setFreezeTicks(0);
            player.setExperienceLevelAndProgress(0);

            AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(20);
                player.setHealthScale(20);
            }

            if (!player.isOp()) {
                player.getInventory().clear();
            }
        }

        if (!player.isOp()) {
            player.setGameMode(Config.isEventIsRunning() ? GameMode.SURVIVAL : GameMode.ADVENTURE);
        }
    }

    public static Location getPlayerAssignedSpawn(Player player) {
        return playerAssignedSpawns.get(player.getUniqueId());
    }
}
