package de.einfachesache.flareonevents.command;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.event.EventState;
import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.handler.GameHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.type.Slab;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB_COMMANDS = Arrays.asList("pvp", "start", "cancel", "reset", "spawncircle", "setspawn");

    FlareonEvents plugin = FlareonEvents.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 0) {
            sendUsage(sender, label);
            return false;
        }

        String sub = args[0].toLowerCase();
        return switch (sub) {
            case "pvp" -> handlePvP(sender);
            case "start" -> handleStart(sender, args);
            case "cancel" -> handleCancel(sender);
            case "reset" -> handleReset(sender, args);
            case "spawncircle" -> handleSpawnCircle(sender, args);
            case "setspawn" -> handleSetSpawn(sender, args);
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

    private boolean handleStart(CommandSender sender, String[] args) {
        if (Config.getEventState().getId() > 0 && !(Config.getEventState() == EventState.ENDED)) {
            sender.sendMessage("§cDas Event wurde bereits gestartet!");
            return false;
        }

        boolean forceStart = false;

        if (args.length == 2) {
            forceStart = Boolean.parseBoolean(args[1]);
        }

        GameHandler.prepareEvent(forceStart);
        sender.sendMessage("§aDu hast das Event erfolgreich " + (forceStart ? "force " : "") + "gestartet!");
        return true;
    }

    private boolean handleCancel(CommandSender sender) {
        if (Config.getEventState().getId() == 0) {
            sender.sendMessage("§cDas Event wurde noch nicht gestartet!");
            return false;
        }
        if (!(sender instanceof ConsoleCommandSender) &&
                !(sender instanceof Player p && p.getUniqueId().equals(FlareonEvents.DEV_UUID))) {
            sender.sendMessage("§cDu darfst diesen Command nicht verwenden.");
            return false;
        }
        GameHandler.cancelEvent();
        sender.sendMessage("§4Du hast das Event erfolgreich gecancelt!");
        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cVerwendung: /event reset <Player|all> [true|false]");
            return false;
        }

        boolean complete = args.length == 3 && Boolean.parseBoolean(args[2]);
        if (!args[1].equalsIgnoreCase("all")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cSpieler nicht gefunden: §6" + args[1]);
                return false;
            }
            GameHandler.resetPlayer(target, true, complete);
            sender.sendMessage("§cDer Spieler §6" + target.getName() + " §cwurde resetet! §7(fullReset=" + complete + ")");
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                GameHandler.resetPlayer(p, true, complete);
            }
            sender.sendMessage("§cAlle Spieler wurden resetet! §7(fullReset=" + complete + ")");
        }
        return true;
    }

    private boolean handleSpawnCircle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl ausführen!");
            return false;
        }
        if (args.length != 1) {
            player.sendMessage("§cVerwendung: /event spawncircle");
            return false;
        }

        int count = 100;
        double gap = 4.0;
        double radius = gap * count / (2 * Math.PI);
        Material mainMaterial = Material.POLISHED_BLACKSTONE;
        Material slapMaterial = Material.OAK_SLAB;


        World world = player.getWorld();
        Slab slabData = (Slab) Bukkit.createBlockData(slapMaterial);
        slabData.setType(Slab.Type.BOTTOM);

        double cx = 0.0, cz = 0.0;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double rx = cx + Math.cos(angle) * radius;
            double rz = cz + Math.sin(angle) * radius;

            int bx = (int) Math.round(rx);
            int bz = (int) Math.round(rz);
            int by = world.getHighestBlockYAt(bx, bz);
            Location loc = new Location(world, bx, by, bz);

            if (world.getBlockAt(loc).getType() != mainMaterial) {
                loc.add(0, 1, 0);
            }

            world.getBlockAt(loc).setType(mainMaterial, false);
            world.getBlockAt(loc.clone().add(1, 0, 0)).setBlockData(slabData, false);
            world.getBlockAt(loc.clone().add(-1, 0, 0)).setBlockData(slabData, false);
            world.getBlockAt(loc.clone().add(0, 0, 1)).setBlockData(slabData, false);
            world.getBlockAt(loc.clone().add(0, 0, -1)).setBlockData(slabData, false);
        }

        player.sendMessage("§a" + count + " Spawnblöcke im Kreis um 0 | 0 (Radius ≈ " + new BigDecimal(radius).setScale(2, RoundingMode.HALF_UP) + " ) gesetzt.");
        return true;
    }

    private boolean handleSetSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl ausführen!");
            return false;
        }

        if (args.length == 2) {
            int slot;
            try {
                slot = Integer.parseInt(args[1]);
                if (slot < 1 || (slot > 100 && !player.getUniqueId().equals(FlareonEvents.DEV_UUID))) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cDas zweite Argument muss eine Zahl zwischen 1 und 100 sein!");
                return true;
            }

            Config.setPlayerSpawnLocation(slot, player.getLocation());
            sender.sendMessage("§aDer Spawn Nr. §e" + slot + " §awurde gesetzt!");
        } else {
            Config.setMainSpawnLocation(player.getLocation());
            sender.sendMessage("§aDer §eMain-Spawn §awurde gesetzt!");
        }
        return true;
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

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(Component.text("--- Verwendung ---", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " start - startet das Event", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " pause - pausiert das Event", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " cancel - bricht das Event ab", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " pvp - schaltet PVP ein/aus", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " reset <Player|all> [true/false] - Spieler(s) resetten", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " spawncircle - Kreis aus Spawnpunkten", NamedTextColor.RED));
        sender.sendMessage(Component.text("/" + label + " setspawn [number] - Spawnpunkt setzen", NamedTextColor.RED));
    }
}
