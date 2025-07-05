package de.einfachesache.flareonEvents.command;

import de.einfachesache.flareonEvents.Config;
import de.einfachesache.flareonEvents.EventState;
import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.listener.PortalCreateListener;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

public class EventCommand implements CommandExecutor {

    FlareonEvents plugin = FlareonEvents.getPlugin();
    List<BukkitTask> tasks = new ArrayList<>();

    Title.Times times = Title.Times.times(
            Duration.ofMillis(1000),  // 20 Ticks = 1 Sekunde (fadeIn)
            Duration.ofMillis(3000),  // 60 Ticks = 3 Sekunden (stay)
            Duration.ofMillis(1000)   // 20 Ticks = 1 Sekunde (fadeOut)
    );
    Sound notifySound = Sound.sound(
            org.bukkit.Sound.ENTITY_ENDER_DRAGON_AMBIENT, // Sound-Key
            Sound.Source.MASTER,                          // Sound-Quelle
            1.0f,                                         // Lautstärke
            1.0f                                          // Tonhöhe
    );
    Sound timerSound = Sound.sound(
            org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING,       // Sound-Key
            Sound.Source.MASTER,                           // Sound-Quelle
            1.0f,                                          // Lautstärke
            1.0f                                           // Tonhöhe
    );

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {

        if(args.length == 0) {
            sendUsage(sender);
            return false;
        }


        if(args.length >= 2  && args[0].equalsIgnoreCase("reset")) {

            Player target = Bukkit.getPlayer(args[1]);

            if(target != null) {
                boolean completeReset = args.length == 3 && safeParseBoolean(args[2]);
                resetPlayer(target, true , completeReset);
                sender.sendMessage("§cDer Spieler §6" + target.getName() + "§c wurde resetet! §7(completeReset=" + completeReset + ")");
                return true;
            }

            if(args.length == 2) {
                boolean completeReset = safeParseBoolean(args[1]);
                Bukkit.getOnlinePlayers().forEach(player -> resetPlayer(player, true , safeParseBoolean(args[1])));
                sender.sendMessage("§cAlle Spieler wurden resetet! §7(completeReset=" + completeReset + ")");

                return true;
            }
        }


        if(args[0].equalsIgnoreCase("start")) {

            if(Config.isEventStarted()) {
               sender.sendMessage("§cEvent wurde bereits gestartet!");
               return false;
            }

            Config.setEventState(EventState.STARTING);

            startEvent();
        }


        if(args[0].equalsIgnoreCase("stop")) {
            if(!Config.isEventStarted()) {
                sender.sendMessage("§cEvent wurde noch nicht gestartet!");
                return false;
            }

            Config.setEventState(EventState.PAUSED);

            stopEvent();
        }


        if(args[0].equalsIgnoreCase("setspawn")) {

            if(!(sender instanceof Player player)) {
                sender.sendMessage("§cDu musst ein Spieler sein!");
                return false;
            }

            if(Config.isEventStarted()) {
                sender.sendMessage("§cEvent wurde bereits gestartet!");
                return false;
            }

            if(args.length == 2) {

                int value;
                try {
                    value = Integer.parseInt(args[1]);
                    if (value < 1 || value > 100) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cDas zweite Argument muss eine Zahl zwischen 1 und 100 sein");
                    return false;
                }

                Location location = player.getLocation();
                Config.setPlayerSpawnLocation(value, location);
                sender.sendMessage("§aSpawn " + value + " wurde gesetzt!");

                return true;
            }


            Location location = player.getLocation();
            Config.setMainSpawnLocation(location);
            sender.sendMessage("§aMainSpawn wurde gesetzt!");
            return true;

        }

        sendUsage(sender);
        return false;
    }

    public static boolean safeParseBoolean(String input) {
        return ("true".equalsIgnoreCase(input) || "false".equalsIgnoreCase(input)) && Boolean.parseBoolean(input);
    }

    private void sendUsage(CommandSender sender){
        sender.sendMessage(Component.text("--- Verwendung ---", NamedTextColor.RED));
        sender.sendMessage(Component.text("/event start", NamedTextColor.RED));
        sender.sendMessage(Component.text("/event stop",  NamedTextColor.RED));
        sender.sendMessage(Component.text("/event reset (player) [true/false]",  NamedTextColor.RED));
        sender.sendMessage(Component.text("/event setspawn [number]",  NamedTextColor.RED));
    }

    public static void resetPlayer(Player player, boolean potionReset, boolean completeReset) {
        Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0.42);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.05f);

        if(potionReset) {
            player.clearActivePotionEffects();
        }

        if(completeReset && player.getGameMode() != GameMode.CREATIVE) {
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.getInventory().clear();
        }

        if(player.isOp() && player.getGameMode() == GameMode.CREATIVE)
            return;

        player.setGameMode(GameMode.SURVIVAL);
    }

    public void startEvent() {

        Config.clearDeathParticipant();

        World world = Bukkit.getWorlds().getFirst();
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0,0);
        border.setSize(3000);
        world.setTime(6000);

        Iterator<Location> locationIterator = Config.getPlayerSpawnLocations().stream().iterator();

        Bukkit.getServer().getOnlinePlayers().stream()
                .filter(player -> !player.isOp())
                .forEach(player -> {
                    if (locationIterator.hasNext()) {
                        Config.addParticipant(player.getUniqueId());
                        player.teleport(locationIterator.next());
                        Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0);
                        player.setGameMode(GameMode.ADVENTURE);
                        player.clearActivePotionEffects();
                        player.setVelocity(new Vector());
                        player.setWalkSpeed(0.0f);
                        player.setFlySpeed(0.0f);
                        player.setFlying(false);

                        player.showTitle(Title.title(
                                Component.text("BEREIT?", NamedTextColor.GREEN),
                                Component.text("Das Event startet in Kürze!", NamedTextColor.YELLOW),
                                times));
                        player.playSound(notifySound);
                    } else {
                        player.sendMessage("❌§4 Kein Spawnpunkt mehr verfügbar.");
                        player.sendMessage("❌§4 Du bist leider nicht dabei");
                        player.kick(Component.text("§4❌ Kein Spawnpunkt mehr verfügbar. \n ❌ Du bist leider nicht dabei"));
                    }
                });

        plugin.getServer().broadcast(
                Component.text("Mach dich bereit, der Countdown läuft. Das Event startet in Kürze!", NamedTextColor.YELLOW));

        startCountdownTimerSchedule();

        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                Config.setEventState(EventState.RUNNING);
                startBorderSchedule(border);
                startPVPSchedule();
                Bukkit.getServer().getOnlinePlayers().stream()
                        .filter(player -> !player.isOp())
                        .forEach(player -> resetPlayer(player, true, true));
            }
        }.runTaskLater(plugin, 2 * 60 * 20L));

        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                PortalCreateListener.setNether(true);
            }
        }.runTaskLater(plugin, 60 * 60 * 20L));
    }

    public void stopEvent(){
        plugin.getServer().getWorlds().forEach(world -> world.setPVP(false));
        PortalCreateListener.setNether(false);
        tasks.forEach(BukkitTask::cancel);
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            player.showTitle(Title.title(
                    Component.text("PAUSE!", NamedTextColor.RED),
                    Component.text("Das Event wurde Pausiert!", NamedTextColor.YELLOW), times));
            player.playSound(notifySound);

            if(!player.isOp()){
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, Integer.MAX_VALUE).withIcon(false).withParticles(false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, Integer.MAX_VALUE).withIcon(false).withParticles(false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, Integer.MAX_VALUE).withIcon(false).withParticles(false));
                Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0);
                player.setVelocity(new Vector());
                player.setWalkSpeed(0.0f);
                player.setFlySpeed(0.0f);
                player.setFlying(false);
            }
        });
        plugin.getServer().broadcast(Component.text("Das Event wurde kurzzeitig Pausiert. Das Event startet in Kürze erneut!", NamedTextColor.YELLOW));
    }


    public void startPVPSchedule(){
        plugin.getServer().getWorlds().forEach(world -> world.setPVP(false));
        plugin.getServer().getOnlinePlayers().forEach(player -> player.sendActionBar(Component.text("PvP beginnt in 2 Minuten", NamedTextColor.DARK_RED)));
        startPVPTimerSchedule();
    }

    public void startCountdownTimerSchedule() {

        final int durationInSeconds = 2 * 60; // 2 Minuten = 120 Sekunden

        tasks.add(new BukkitRunnable() {
            int secondsLeft = durationInSeconds;

            @Override
            public void run() {
                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    if (secondsLeft <= 0 || !player.isOnline()) {
                        player.sendActionBar(Component.text("Event ist gestartet", NamedTextColor.DARK_RED));
                        player.showTitle(Title.title(
                                Component.text("ACHTUNG", NamedTextColor.RED),
                                Component.text("Event ist gestartet", NamedTextColor.YELLOW),
                                times));
                        player.playSound(notifySound);

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
                        player.playSound(timerSound);
                    }

                    player.sendActionBar(Component.text(timeFormatted));
                });
                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L)); // Jede Sekunde (20 Ticks)
    }

    public void startPVPTimerSchedule() {

        final int durationInSeconds = 2 * 60; // 2 Minuten = 120 Sekunden

        tasks.add(new BukkitRunnable() {
            int secondsLeft = durationInSeconds;

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
        }.runTaskTimer(plugin, 0L, 20L)); // Jede Sekunde (20 Ticks)
    }


    public void startBorderSchedule(WorldBorder border){

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
    }
}
