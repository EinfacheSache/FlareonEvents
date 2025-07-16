package de.einfachesache.flareonEvents;

import de.einfachesache.flareonEvents.listener.PlayerDeathListener;
import de.einfachesache.flareonEvents.listener.PortalCreateListener;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;

public class EventHandler {

    static final FlareonEvents plugin = FlareonEvents.getPlugin();
    static final List<BukkitTask> tasks = new ArrayList<>();
    static final Map<UUID, Location> playerAssignedSpawns = new HashMap<>();

    static final int preparingTime = 3 * 60; // DEFAULT: 3 * 60
    static final int startingTime = 2 * 60; // DEFAULT: 2 * 60
    static final int noPvPTime = 2 * 60; // DEFAULT: 2 * 60
    static final int netherOpenTime = 60 * 60; // DEFAULT: 60 * 60

    static final Title.Times times = Title.Times.times(
            Duration.ofMillis(1000),  // 20 Ticks = 1 Sekunde (fadeIn)
            Duration.ofMillis(3000),  // 60 Ticks = 3 Sekunden (stay)
            Duration.ofMillis(1000)   // 20 Ticks = 1 Sekunde (fadeOut)
    );
    static final net.kyori.adventure.sound.Sound startSound = net.kyori.adventure.sound.Sound.sound(
            org.bukkit.Sound.ENTITY_ENDER_DRAGON_AMBIENT,  // Sound-Key
            net.kyori.adventure.sound.Sound.Source.MASTER, // Sound-Quelle
            1.0f,                                          // Lautstärke
            1.0f                                           // Tonhöhe
    );
    static final net.kyori.adventure.sound.Sound notifySound = net.kyori.adventure.sound.Sound.sound(
            org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING,       // Sound-Key
            Sound.Source.MASTER,                           // Sound-Quelle
            1.0f,                                          // Lautstärke
            1.0f                                           // Tonhöhe
    );

    public static void prepareEvent() {

        tasks.forEach(BukkitTask::cancel);

        Config.setEventState(EventState.PREPARING);
        Config.clearParticipant();
        Config.clearDeathParticipant();
        Config.setStopSince(0);

        PlayerDeathListener.getPvpKillCounts().clear();
        PortalCreateListener.setNether(false);

        playerAssignedSpawns.clear();

        plugin.getServer().getWorlds().forEach(world -> world.setPVP(false));

        World world = Bukkit.getWorlds().getFirst();
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(3000);
        world.setTime(6000);
        world.setClearWeatherDuration(20 * 60 * 20);

        Bukkit.getServer().getOnlinePlayers().forEach(player -> {
            player.showTitle(Title.title(
                    Component.text("BEREIT?", NamedTextColor.GREEN),
                    Component.text("Das Event startet in " + (preparingTime + startingTime) / 60 + " Minuten!", NamedTextColor.YELLOW),
                    times));
            player.playSound(notifySound);
            resetPlayer(player, true, false);
        });

        eventStartScheduler();
        startCountdownTimerSchedule();
    }

    private static void eventStartScheduler() {

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
                                player.teleport(playerSpawnLocation);
                                playerAssignedSpawns.put(player.getUniqueId(), playerSpawnLocation);

                                Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0);
                                player.setGameMode(GameMode.ADVENTURE);
                                player.clearActivePotionEffects();
                                player.setVelocity(new Vector());
                                player.setWalkSpeed(0.0f);
                                player.setFlySpeed(0.0f);
                                player.setFlying(false);

                                player.showTitle(Title.title(
                                        Component.text("ACHTUNG!", NamedTextColor.GREEN),
                                        Component.text("Event Start in Kürze!", NamedTextColor.YELLOW),
                                        times));
                                player.playSound(notifySound);

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
                        startBorderSchedule(Bukkit.getWorlds().getFirst().getWorldBorder());

                        Bukkit.getServer().getOnlinePlayers().forEach(
                                player -> resetPlayer(player, true, true));
                    }
                }.runTaskLater(plugin, startingTime * 20L));

            }
        }.runTaskLater(plugin, preparingTime * 20L));
    }

    public static void stopEvent() {

        tasks.forEach(BukkitTask::cancel);

        Config.setEventState(EventState.NOT_RUNNING);
        Config.setStopSince(System.currentTimeMillis());

        PortalCreateListener.setNether(false);

        plugin.getServer().getWorlds().forEach(world -> world.setPVP(false));
        plugin.getServer().getOnlinePlayers().forEach(player -> {

            player.showTitle(Title.title(
                    Component.text("STOP!", NamedTextColor.RED),
                    Component.text("Das Event wurde Pausiert!", NamedTextColor.YELLOW), times));
            player.playSound(notifySound);

            player.teleport(Config.getMainSpawnLocation());
            resetPlayer(player, true, true);

            /* if (!player.isOp()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, Integer.MAX_VALUE).withIcon(false).withParticles(false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, PotionEffect.INFINITE_DURATION, Integer.MAX_VALUE).withIcon(false).withParticles(false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, Integer.MAX_VALUE).withIcon(false).withParticles(false));
                Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0);
                player.setVelocity(new Vector());
                player.setWalkSpeed(0.0f);
                player.setFlySpeed(0.0f);
                player.setFlying(false);
            }  */
        });
        plugin.getServer().broadcast(Component.text("Das Event wurde kurzzeitig Pausiert. Das Event startet in Kürze erneut!", NamedTextColor.YELLOW));
    }


    private static void startCountdownTimerSchedule() {

        tasks.add(new BukkitRunnable() {
            int secondsLeft = preparingTime + startingTime;

            @Override
            public void run() {
                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    if (secondsLeft <= 0 || !player.isOnline()) {
                        player.sendActionBar(Component.text("Event ist gestartet", NamedTextColor.DARK_RED));
                        player.showTitle(Title.title(
                                Component.text("ACHTUNG", NamedTextColor.RED),
                                Component.text("Event ist gestartet", NamedTextColor.YELLOW),
                                times));
                        player.sendMessage(Component.text(
                                """
                                        §6[Event] §fDas Event startet jetzt!
                                        §aViel Spaß und viel Erfolg!
                                        §ePvP beginnt in §c2 Minuten§e!
                                        §5Der Nether öffnet in §c60 Minuten§5!"""
                        ));
                        player.playSound(startSound);

                        this.cancel();
                        return;
                    }

                    int minutes = secondsLeft / 60;
                    int seconds = secondsLeft % 60;
                    String timeFormatted = String.format("§eStart in §c%d:%02d §eMinuten", minutes, seconds);

                    if (secondsLeft == 120 || secondsLeft == 60 || secondsLeft == 30 || secondsLeft <= 10) {
                        player.showTitle(Title.title(
                                Component.text(secondsLeft <= 60 ? String.format("§eNoch §a%d §eSekunden", secondsLeft) : String.format("§eNoch §a%d §eMinuten", minutes), NamedTextColor.RED),
                                Component.text(secondsLeft == 120 ? "Mache dich bereit!" : "", NamedTextColor.YELLOW),
                                times));
                        player.playSound(notifySound);
                    }

                    player.sendActionBar(Component.text(timeFormatted));
                });
                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L));
    }

    private static void startPvPSchedule() {

        plugin.getServer().getOnlinePlayers().forEach(player -> player.sendActionBar(Component.text("PvP beginnt in " + (noPvPTime) + " Minuten", NamedTextColor.DARK_RED)));

        tasks.add(new BukkitRunnable() {
            int secondsLeft = noPvPTime;

            @Override
            public void run() {
                plugin.getServer().getOnlinePlayers().forEach(player -> {

                    if (secondsLeft <= 0 || !player.isOnline()) {
                        player.getWorld().setPVP(true);
                        player.sendActionBar(Component.text("PVP ist ab jetzt möglich", NamedTextColor.DARK_RED));
                        player.showTitle(Title.title(
                                Component.text("ACHTUNG", NamedTextColor.RED),
                                Component.text("PVP ist ab jetzt möglich", NamedTextColor.YELLOW),
                                times));
                        player.playSound(notifySound);

                        this.cancel();
                        return;
                    }

                    int minutes = secondsLeft / 60;
                    int seconds = secondsLeft % 60;
                    String timeFormatted = String.format("§ePVP in §c%d:%02d §eMinuten", minutes, seconds);

                    player.sendActionBar(Component.text(timeFormatted));
                });
                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L));
    }

    private static void startNetherOpenSchedule() {

        tasks.add(new BukkitRunnable() {
            int secondsLeft = netherOpenTime - noPvPTime;

            @Override
            public void run() {
                plugin.getServer().getOnlinePlayers().forEach(player -> {

                    if (secondsLeft <= 0 || !player.isOnline()) {

                        PortalCreateListener.setNether(true);
                        player.sendActionBar(Component.text("Nether ist nun geöffnet", NamedTextColor.DARK_RED));
                        player.playSound(notifySound);

                        this.cancel();
                        return;
                    }

                    int minutes = secondsLeft / 60;
                    int seconds = secondsLeft % 60;
                    String timeFormatted = String.format("§eNether öffnet in §c%d:%02d §eMinuten", minutes, seconds);

                    player.sendActionBar(Component.text(timeFormatted));
                });
                secondsLeft--;
            }
        }.runTaskTimer(plugin, noPvPTime * 20L, 20L));
    }


    private static void startBorderSchedule(WorldBorder border) {

        // Nach 40 Minuten (40 * 60 * 20 Ticks)
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                // Verkleinere in 20 Minuten (20 * 60 Sekunden) auf 2000
                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    player.showTitle(Title.title(
                            Component.text("ACHTUNG", NamedTextColor.RED),
                            Component.text("Die Border bewegt sich nun zu 2000x2000", NamedTextColor.YELLOW),
                            times));
                    player.playSound(notifySound);
                });
                plugin.getServer().broadcast(
                        Component.text("Pass auf! Die Border hat sich in Bewegung gesetzt. Das Gebiet wird nun auf 2000x2000 Blöcke begrenzt!", NamedTextColor.YELLOW));
                border.setSize(2000, 20 * 60);
            }
        }.runTaskLater(plugin, 40 * 60 * 20L));

        // Nach 90 Minuten (30 min + 20 min + 40 min)
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                // Verkleinere in 30 Minuten auf 1500
                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    player.showTitle(Title.title(
                            Component.text("ACHTUNG", NamedTextColor.RED),
                            Component.text("Die Border bewegt sich nun zu 1500x1500", NamedTextColor.YELLOW),
                            times));
                    player.playSound(notifySound);
                });
                plugin.getServer().broadcast(
                        Component.text("Pass auf! Die Border hat sich in Bewegung gesetzt. Das Gebiet wird nun auf 1500x1500 Blöcke begrenzt!", NamedTextColor.YELLOW));
                border.setSize(1500, 30 * 60);
            }
        }.runTaskLater(plugin, 90 * 60 * 20L));

        // Nach 140 Minuten (20min + 30min + 30 min + 20 min + 40 min)
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                // Verkleinere in 40 Minuten auf 200
                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    player.showTitle(Title.title(
                            Component.text("ACHTUNG", NamedTextColor.RED),
                            Component.text("Die Border bewegt sich nun zu 200x200", NamedTextColor.YELLOW),
                            times));
                    player.playSound(notifySound);
                });
                plugin.getServer().broadcast(
                        Component.text("Pass auf! Die Border hat sich in Bewegung gesetzt. Das Gebiet wird nun auf 200x200 Blöcke begrenzt!", NamedTextColor.YELLOW));
                border.setSize(200, 40 * 60);
            }
        }.runTaskLater(plugin, 140 * 60 * 20L));

        // Nach 210 Minuten (30min + 40min + 20min + 30min + 30 min + 20 min + 40 min)
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                // Verkleinere in 10 Minuten auf 50
                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    player.showTitle(Title.title(
                            Component.text("ACHTUNG", NamedTextColor.RED),
                            Component.text("Die Border bewegt sich nun zu 50x50", NamedTextColor.YELLOW),
                            times));
                    player.playSound(notifySound);
                });
                plugin.getServer().broadcast(
                        Component.text("Pass auf! Die Border hat sich in Bewegung gesetzt. Das Gebiet wird nun auf 50x50 Blöcke begrenzt!", NamedTextColor.YELLOW));
                border.setSize(50, 10 * 60);
            }
        }.runTaskLater(plugin, 210 * 60 * 20L));

        // Nach 240 Minuten (20min + 10min + 30min + 40min + 20min + 30min + 30 min + 20 min + 40 min)
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                // Verkleinere in 5 Minuten auf 5
                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    player.showTitle(Title.title(
                            Component.text("ACHTUNG", NamedTextColor.RED),
                            Component.text("Die Border bewegt sich nun zu 5x5", NamedTextColor.YELLOW),
                            times));
                    player.playSound(notifySound);
                });
                plugin.getServer().broadcast(
                        Component.text("Pass auf! Die Border hat sich in Bewegung gesetzt. Das Gebiet wird nun auf 5x5 Blöcke begrenzt!", NamedTextColor.YELLOW));
                border.setSize(5, 5 * 60);
            }
        }.runTaskLater(plugin, 240 * 60 * 20L));

        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getServer().broadcast(
                        Component.text("Die Border ist stehen geblieben. Das Gebiet bleibt nun auf 5x5 Blöcke begrenzt!", NamedTextColor.YELLOW));
            }
        }.runTaskLater(plugin, 245 * 60 * 20L));
    }

    public static void resetPlayer(Player player, boolean potionReset, boolean completeReset) {

        if (potionReset) {
            player.clearActivePotionEffects();
        }

        if (Config.getEventState() != EventState.STARTING || completeReset) {
            Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0.42);
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.05f);
        }

        if (player.isOp()) return;

        player.setGameMode(Config.isEventIsRunning() ? GameMode.SURVIVAL : GameMode.ADVENTURE);

        if (completeReset) {
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(10);
            player.getInventory().clear();
        }
    }

    public static Location getPlayerAssignedSpawn(Player player) {
        return playerAssignedSpawns.get(player.getUniqueId());
    }
}
