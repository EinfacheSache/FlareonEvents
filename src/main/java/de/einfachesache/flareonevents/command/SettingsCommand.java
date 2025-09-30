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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB_COMMANDS = Arrays.asList("PVP", "NETHER", "VIEW_DISTANZ", "SIMULATION_DISTANZ");

    FlareonEvents plugin = FlareonEvents.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 0) {
            sendUsage(sender, label);
            return false;
        }

        String sub = args[0].toUpperCase();
        return switch (sub) {
            case "PVP" -> handlePvP(sender);
            case "NETHER" -> handleNether(sender);
            case "VIEW_DISTANZ" -> handleViewDistanz(sender, args);
            case "SIMULATION_DISTANZ" -> handleSimulationDistanz(sender, args);
            default -> {
                sendUsage(sender, label);
                yield false;
            }
        };
    }

    private boolean handlePvP(CommandSender sender) {
        boolean pvp = Bukkit.getWorlds().getFirst().getPVP();
        plugin.getServer().getWorlds().forEach(world -> world.setPVP(!pvp));
        sender.sendMessage("§cPVP wurde erfolgreich " + (!pvp ? "§aaktiviert!" : "§cdeaktiviert!"));
        return true;
    }

    private boolean handleNether(CommandSender sender) {
        boolean netherEnabled = PortalCreateListener.netherEnabled();
        PortalCreateListener.setNether(!netherEnabled);
        sender.sendMessage("§cDer Nether wurde erfolgreich " + (!netherEnabled ? "§aaktiviert!" : "§cdeaktiviert!"));
        return true;
    }

    private boolean handleViewDistanz(CommandSender sender, String[] args) {
        int distanz;
        try {
            distanz = Integer.parseInt(args[1]);
            Bukkit.getWorlds().forEach(world -> world.setViewDistance(distanz));
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            sender.sendMessage("§cBitte gebe eine gültige Distanz an /setting VIEW_DISTANZ 2-32");
            return false;
        }

        sender.sendMessage("§aDie View Distanz wurde erfolgreich auf §e" + distanz + " §agesetzt");
        return true;
    }

    private boolean handleSimulationDistanz(CommandSender sender, String[] args) {
        int distanz;
        try {
            distanz = Integer.parseInt(args[1]);
            Bukkit.getWorlds().forEach(world -> world.setSimulationDistance(distanz));
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            sender.sendMessage("§cBitte gebe eine gültige Distanz an /setting SIMULATION_DISTANZ 2-32");
            return false;
        }

        sender.sendMessage("§aDie Simulation Distanz wurde erfolgreich auf §e" + distanz + " §agesetzt");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toUpperCase();
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

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(Component.text("--- Verwendung ---", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " PVP - schaltet PVP ein/aus", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " NETHER - schaltet Nether ein/aus", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " VIEW_DISTANZ - stelle die view distanz ein", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " SIMULATION_DISTANZ - stelle die simulation distanz ein", NamedTextColor.RED));
    }
}
