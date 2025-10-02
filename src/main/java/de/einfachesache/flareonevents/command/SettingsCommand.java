package de.einfachesache.flareonevents.command;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.listener.PortalCreateListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

public class SettingsCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB_COMMANDS = Arrays.asList("PVP", "NETHER", "VIEW_DISTANCE", "SIMULATION_DISTANCE");

    FlareonEvents plugin = FlareonEvents.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 0) {
            sendUsage(sender, label);
            return false;
        }

        String sub = args[0].toUpperCase();
        return switch (sub) {
            case "PVP" -> handlePvP(sender, args);
            case "NETHER" -> handleNether(sender, args);
            case "VIEW_DISTANCE" -> handleViewDistanz(sender, args);
            case "SIMULATION_DISTANCE" -> handleSimulationDistanz(sender, args);
            default -> {
                sendUsage(sender, label);
                yield false;
            }
        };
    }

    private boolean handlePvP(CommandSender sender, String[] args) {
        boolean pvp = Bukkit.getWorlds().getFirst().getPVP();

        if (args.length != 2) {
            sender.sendMessage("§aDas §aPVP ist aktuell " + (pvp ? "§aaktiviert!" : "§cdeaktiviert!"));
        } else {
            plugin.getServer().getWorlds().forEach(world -> world.setPVP(!pvp));
            sender.sendMessage("§aDas §ePVP §awurde erfolgreich " + (!pvp ? "§aaktiviert!" : "§cdeaktiviert!"));
        }

        return true;
    }

    private boolean handleNether(CommandSender sender, String[] args) {
        boolean netherEnabled = PortalCreateListener.netherEnabled();

        if (args.length != 2) {
            sender.sendMessage("§aDer §cNether §aist aktuell " + (netherEnabled ? "§aaktiviert!" : "§cdeaktiviert!"));
        } else {
            PortalCreateListener.setNether(!netherEnabled);
            sender.sendMessage("§aDer §cNether §awurde erfolgreich " + (!netherEnabled ? "§aaktiviert!" : "§cdeaktiviert!"));
        }
        return true;
    }

    private boolean handleViewDistanz(CommandSender sender, String[] args) {

        if (args.length != 2) {
            sender.sendMessage("§aDie aktuelle §eView Distance §aist bei §e" + Bukkit.getWorlds().getFirst().getSendViewDistance() + "§a Chucks.");
            return true;
        }

        int distance;
        try {
            distance = Integer.parseInt(args[1]);
            Bukkit.getWorlds().forEach(world -> world.setViewDistance(distance));
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            sender.sendMessage("§cBitte gebe eine gültige Distance an /setting VIEW_DISTANCE 2-32");
            return false;
        }

        sender.sendMessage("§aDie §eView Distance §awurde erfolgreich auf §e" + distance + " §agesetzt");
        return true;
    }

    private boolean handleSimulationDistanz(CommandSender sender, String[] args) {

        if (args.length != 2) {
            sender.sendMessage("§aDie aktuelle §eSimulation Distance §aist bei §e" + Bukkit.getWorlds().getFirst().getSimulationDistance() + "§a Chucks.");
            return true;
        }

        int distance;
        try {
            distance = Integer.parseInt(args[1]);
            Bukkit.getWorlds().forEach(world -> world.setSimulationDistance(distance));
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            sender.sendMessage("§cBitte gebe eine gültige Distance an /setting SIMULATION_DISTANCE 2-32");
            return false;
        }

        sender.sendMessage("§aDie §eSimulation Distance §awurde erfolgreich auf §e" + distance + " §agesetzt");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {

        if (args.length == 1) {
            String input = args[0].toUpperCase(Locale.ROOT);
            return SUB_COMMANDS.stream()
                    .filter(sub -> sub.startsWith(input))
                    .sorted()
                    .toList();
        }

        if (args.length == 2) {
            String firstInputUpperCase = args[0].toUpperCase(Locale.ROOT);
            String secondInput = args[1];

            if (firstInputUpperCase.equals("PVP") || firstInputUpperCase.equals("NETHER")) {
                return "TOGGLE".startsWith(secondInput.toUpperCase(Locale.ROOT))
                        ? List.of("toggle")
                        : Collections.emptyList();
            }

            if (firstInputUpperCase.equals("VIEW_DISTANCE") || firstInputUpperCase.equals("SIMULATION_DISTANCE")) {
                return IntStream.rangeClosed(2, 32)
                        .mapToObj(i -> String.format("%02d", i))
                        .filter(s -> s.startsWith(secondInput))
                        .toList();
            }
        }

        return Collections.emptyList();
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(Component.text("--- Verwendung ---", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " PVP - schaltet PVP ein/aus", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " NETHER - schaltet Nether ein/aus", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " VIEW_DISTANZ - stelle die view distanz ein", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " SIMULATION_DISTANZ - stelle die simulation distanz ein", NamedTextColor.RED));
    }
}
