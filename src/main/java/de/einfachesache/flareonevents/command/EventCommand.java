package de.einfachesache.flareonevents.command;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.handler.GameHandler;
import de.einfachesache.flareonevents.FlareonEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB_COMMANDS = Arrays.asList("start", "pause", "cancel", "pvp", "reset", "setspawn");

    FlareonEvents plugin = FlareonEvents.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 0) {
            sendUsage(sender, label);
            return false;
        }


        if (args[0].equalsIgnoreCase("pvp")) {

            boolean pvp = Bukkit.getWorlds().getFirst().getPVP();
            plugin.getServer().getWorlds().forEach(world -> world.setPVP(!pvp));
            sender.sendMessage("§cPVP wurde erfolgreich " + (!pvp ? "§aaktiviert!" : "§cdeaktiviert!"));

            return true;
        }


        if (args[0].equalsIgnoreCase("start")) {

            if (Config.getEventState().getId() > 0) {
                sender.sendMessage("§cDas Event wurde bereits gestartet!");
                return false;
            }

            GameHandler.prepareEvent();

            sender.sendMessage("§aDu hast das Event erfolgreich gestartet!");

            return true;
        }


        if (args[0].equalsIgnoreCase("pause")) {

            /* if (Config.getEventState().getId() == 0) {
                sender.sendMessage("§cEvent wurde noch nicht gestartet!");
                return false;
            }
            EventHandler.pauseEvent();

            sender.sendMessage("§cDu hast das Event erfolgreich pausiert!");
            */

            sender.sendMessage("§cDieser Command ist §4NOCH NICHT§c verfügbar!");

            return true;
        }


        if (args[0].equalsIgnoreCase("cancel")) {

            if (Config.getEventState().getId() == 0) {
                sender.sendMessage("§cDas Event wurde noch nicht gestartet!");
                return false;
            }

            if (!(sender instanceof ConsoleCommandSender) && !(sender instanceof Player player && player.getUniqueId().equals(FlareonEvents.DEV_UUID))) {
                sender.sendMessage("§cDu darfst diesen Command nicht verwenden.");
                return false;
            }

            GameHandler.cancelEvent();

            sender.sendMessage("§4Du hast das Event erfolgreich gecancelt!");

            return true;
        }


        if (args.length >= 2 && args[0].equalsIgnoreCase("reset")) {

            Player target = Bukkit.getPlayer(args[1]);

            if (target != null) {
                boolean completeReset = args.length == 3 && safeParseBoolean(args[2]);
                GameHandler.resetPlayer(target, true, completeReset);
                sender.sendMessage("§cDer Spieler §6" + target.getName() + "§c wurde resetet! §7(totalReset=" + completeReset + ")");
                return true;
            }

            if (args.length == 2 || args.length == 3) {
                boolean completeReset = (args.length == 3 && safeParseBoolean(args[2]));
                Bukkit.getOnlinePlayers().forEach(player -> GameHandler.resetPlayer(player, true, completeReset));
                sender.sendMessage("§cAlle Spieler wurden resetet! §7(totalReset=" + completeReset + ")");
                return true;
            }
        }


        if (args[0].equalsIgnoreCase("setspawn")) {

            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cDu musst ein Spieler sein!");
                return false;
            }

            if (args.length == 2) {

                int value;
                try {
                    value = Integer.parseInt(args[1]);
                    if (value < 1 || (value > 100 && !FlareonEvents.DEV_UUID.equals(player.getUniqueId())))
                        throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cDas zweite Argument muss eine Zahl zwischen §e1§c und §e100§c sein");
                    return false;
                }

                Location location = player.getLocation();
                Config.setPlayerSpawnLocation(value, location);
                sender.sendMessage("§aDer Spawn Nr. " + (value <= 100 ? "§e" : "§4") + value + "§a wurde gesetzt!");

                return true;
            }

            Location location = player.getLocation();
            Config.setMainSpawnLocation(location);
            sender.sendMessage("§aDer §eMain-Spawn§a wurde gesetzt!");
            return true;

        }

        sendUsage(sender, label);

        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String sub : SUB_COMMANDS) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
            return completions;
        }
        return new ArrayList<>();
    }

    public static boolean safeParseBoolean(String input) {
        return ("true".equalsIgnoreCase(input) || "false".equalsIgnoreCase(input)) && Boolean.parseBoolean(input);
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(Component.text("--- Verwendung ---", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " start", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " pause", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " cancel", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " pvp", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " reset (player) [true/false]", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " setspawn [number]", NamedTextColor.RED));
    }
}
