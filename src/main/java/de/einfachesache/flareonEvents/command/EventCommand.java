package de.einfachesache.flareonEvents.command;

import de.einfachesache.flareonEvents.FlareonEvents;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class EventCommand implements CommandExecutor {

    FlareonEvents plugin = FlareonEvents.getPlugin();
    List<BukkitTask> tasks = new ArrayList<>();
    boolean eventStarted = false;

    Title.Times times = Title.Times.times(
            Duration.ofMillis(1000),  // 20 Ticks = 1 Sekunde (fadeIn)
            Duration.ofMillis(3000),  // 60 Ticks = 3 Sekunden (stay)
            Duration.ofMillis(1000)   // 20 Ticks = 1 Sekunde (fadeOut)
    );
    Sound notifySound = Sound.sound(
            org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, // Sound-Key
            Sound.Source.MASTER,                                 // Sound-Quelle
            1.0f,                                          // Lautstärke
            1.0f                                           // Tonhöhe
    );

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length == 1 && args[0].equalsIgnoreCase("start")) {
            if(eventStarted) {
               sender.sendMessage("§cEvent wurde bereits gestartet!");
               return false;
            }
            eventStarted = true;

            startBorderSchedule();
            startPVPSchedule();
        }

        if(args.length == 1 && args[0].equalsIgnoreCase("stop")) {
            if(!eventStarted) {
                sender.sendMessage("§cEvent wurde noch nicht gestartet!");
                return false;
            }
            eventStarted = false;

            stopEvent();
        }
        return false;
    }

    public void stopEvent(){
        plugin.getServer().getWorlds().forEach(world -> {world.setPVP(false);});
        tasks.forEach(BukkitTask::cancel);
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            player.showTitle(Title.title(
                    Component.text("PAUSE!", NamedTextColor.RED),
                    Component.text("Das Event wurde Pausiert!", NamedTextColor.YELLOW), times));
            player.playSound(notifySound);
        });
        plugin.getServer().broadcast(Component.text("Das Event wurde kurzzeitig Pausiert. Das Event startet in Kürze erneut!", NamedTextColor.YELLOW));
    }


    public void startPVPSchedule(){

        plugin.getServer().getWorlds().forEach(world -> {world.setPVP(false);});
        plugin.getServer().getOnlinePlayers().forEach(player -> {player.sendActionBar(Component.text("PvP beginnt in 2 Minuten", NamedTextColor.DARK_RED));});

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getServer().getWorlds().forEach(world -> {world.setPVP(true);});
                plugin.getServer().getOnlinePlayers().forEach(player -> {player.sendActionBar(Component.text("PvP beginnt nun", NamedTextColor.DARK_RED));});
            }
        }.runTaskLater(plugin, 2 * 60 * 20L);
    }


    public void startBorderSchedule(){
        World world = Bukkit.getWorld("world");
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0,0);
        border.setSize(3000);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(1000),  // 20 Ticks = 1 Sekunde (fadeIn)
                Duration.ofMillis(3000),  // 60 Ticks = 3 Sekunden (stay)
                Duration.ofMillis(1000)   // 20 Ticks = 1 Sekunde (fadeOut)
        );
        Sound notifySound = Sound.sound(
                org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, // Sound-Key
                Sound.Source.MASTER,                                 // Sound-Quelle
                1.0f,                                          // Lautstärke
                1.0f                                           // Tonhöhe
        );

        plugin.getServer().getOnlinePlayers().forEach(player -> {
            player.showTitle(Title.title(
                    Component.text("BEREIT?", NamedTextColor.GREEN),
                    Component.text("Das Event startet in Kürze!", NamedTextColor.YELLOW),
                    times));
            player.playSound(notifySound);
        });
        plugin.getServer().broadcast(
                Component.text("Mach dich bereit, der Countdown läuft. Das Event startet in Kürze!", NamedTextColor.YELLOW));


        // Nach 40 Minuten (40 * 60 * 20 Ticks)
        BukkitTask task1 = new BukkitRunnable() {
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
        }.runTaskLater(plugin, 40 * 60 * 20L);

        // Nach 90 Minuten (30 min + 20 min + 40 min)
        BukkitTask task2 = new BukkitRunnable() {
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
        }.runTaskLater(plugin, 90 * 60 * 20L);

        // Nach 140 Minuten (20min + 30min + 30 min + 20 min + 40 min)
        BukkitTask task3 = new BukkitRunnable() {
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
        }.runTaskLater(plugin, 140 * 60 * 20L);

        // Nach 210 Minuten (30min + 40min + 20min + 30min + 30 min + 20 min + 40 min)
        BukkitTask task4 = new BukkitRunnable() {
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
        }.runTaskLater(plugin, 210 * 60 * 20L);

        // Nach 240 Minuten (20min + 10min + 30min + 40min + 20min + 30min + 30 min + 20 min + 40 min)
        BukkitTask task5 = new BukkitRunnable() {
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
        }.runTaskLater(plugin, 240 * 60 * 20L);

        tasks.add(task1);
        tasks.add(task2);
        tasks.add(task3);
        tasks.add(task4);
        tasks.add(task5);
    }
}
