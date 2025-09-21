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

    public record BorderPhase(Duration at, int targetSize, Duration shrink) {}

    static final List<BorderPhase> borderPhases = List.of(
            new BorderPhase(Duration.ofMinutes(30),  2500, Duration.ofMinutes(10)),
            new BorderPhase(Duration.ofMinutes(60),  1800, Duration.ofMinutes(15)),
            new BorderPhase(Duration.ofMinutes(90),  1200, Duration.ofMinutes(15)),
            new BorderPhase(Duration.ofMinutes(120),  600, Duration.ofMinutes(20)),
            new BorderPhase(Duration.ofMinutes(150),  200, Duration.ofMinutes(15)),
            new BorderPhase(Duration.ofMinutes(165),   50, Duration.ofMinutes(10)),
            new BorderPhase(Duration.ofMinutes(175),   10, Duration.ofMinutes(5))
    );

    static String winner = "§cNO WINNER§6";

    static String secondWord(int n) {
        return n <= 1 ? "Sekunde" : "Sekunden";
    }

    static String minuteWord(int n) {
        return n <= 1 ? "Minute" : "Minuten";
    }

    static final Title.Times times = Title.Times.times(
            Duration.ofMillis(1000),  // 1 Sekunde (fadeIn)
            Duration.ofMillis(3000),  // 3 Sekunden (stay)
            Duration.ofMillis(1000)   // 1 Sekunde (fadeOut)
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

        plugin.getServer().getWorlds().forEach(world -> {
            world.setPVP(false);
            world.getWorldBorder().setSize(3000);
        });

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

        plugin.getServer().getWorlds().forEach(world -> {
            world.setPVP(false);
            world.getWorldBorder().setSize(world.getWorldBorder().getSize());
        });
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
            player.getInventory().setItem(8, EventInfoBook.create());

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

    public static void startBorderSchedule() {
        for (var p : borderPhases) {
            long delayTicks = p.at().toSeconds() * 20L;
            tasks.add(Bukkit.getScheduler().runTaskLater(plugin, () -> {
                var worlds = Bukkit.getWorlds();
                moveBorder(worlds, p.targetSize(), (int) p.shrink().toMinutes());
            }, delayTicks));
        }

        Duration end = borderPhases.stream()
                .map(p -> p.at().plus(p.shrink()))
                .max(Duration::compareTo)
                .orElse(Duration.ZERO);

        tasks.add(Bukkit.getScheduler().runTaskLater(plugin, () ->
                plugin.getServer().broadcast(Component.
                        text("Die Border ist stehen geblieben. Das Gebiet bleibt nun auf 10×10 Blöcke begrenzt!", NamedTextColor.YELLOW)
        ), end.toSeconds() * 20L));
    }

    private static void moveBorder(List<World> worlds, int size, int delayInMinutes) {

        long totalSec = Math.max(0, TimeUnit.MINUTES.toSeconds(delayInMinutes));
        long endAt = System.currentTimeMillis() + (totalSec * 1000L);
        double startSize = worlds.isEmpty()
                ? size
                : worlds.getFirst().getWorldBorder().getSize();

        Title title = Title.title(
                Component.text("ACHTUNG", NamedTextColor.RED),
                Component.text("Border → " + size + "×" + size + " in " + inMinuteString(delayInMinutes), NamedTextColor.YELLOW),
                times
        );

        plugin.getServer().getOnlinePlayers().forEach(player -> {
            player.showTitle(title);
            player.playSound(EventSound.NOTIFY.adventure());
            player.sendMessage(Component.text(
                    "Border schrumpft: " + (int) Math.round(startSize) + " → " + size +
                            " | Dauer " + inMinuteString(delayInMinutes) +
                            " | Ende ~" + toHourMinuteFormat(endAt),
                    NamedTextColor.RED
            ));
        });

        worlds.forEach(world ->
                world.getWorldBorder().setSize(size, TimeUnit.MINUTES, delayInMinutes)
        );
    }

    private static String inMinuteString(long min) {
        return min + " min";
    }

    private static String toHourMinuteFormat(long epochMillis) {
        var t = java.time.Instant.ofEpochMilli(epochMillis)
                .atZone(java.time.ZoneId.systemDefault()).toLocalTime();
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    public static void resetPlayer(Player player, boolean potionReset, boolean completeReset) {
        if (potionReset) {
            player.setGlowing(false);
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

        if (!player.isOp() && !player.isDead()) {
            player.setGameMode(Config.isEventIsRunning() ? GameMode.SURVIVAL : GameMode.ADVENTURE);
        }
    }

    public static Location getPlayerAssignedSpawn(Player player) {
        return playerAssignedSpawns.get(player.getUniqueId());
    }
}
