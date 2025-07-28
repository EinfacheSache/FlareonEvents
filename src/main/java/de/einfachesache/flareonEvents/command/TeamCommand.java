package de.einfachesache.flareonEvents.command;

import de.einfachesache.flareonEvents.Config;
import de.einfachesache.flareonEvents.handler.TeamHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TeamCommand implements CommandExecutor, TabCompleter, Listener {

    private static final List<String> SUB_COMMANDS = Arrays.asList("invite", "accept", "leave", "list", "kick");


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Dieser Command kann nur von Spielern verwendet werden!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player, alias);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "invite":
                TeamHandler.handleInviteCommand(player, args);
                break;
            case "accept":
                TeamHandler.handleAcceptCommand(player, args);
                break;
            case "leave":
                TeamHandler.handleLeaveCommand(player, false);
                break;
            case "list":
                TeamHandler.handleListCommand(player, args);
                break;
            case "kick":
                TeamHandler.handleKickCommand(player, args);
                break;
            default:
                sendHelpMessage(player, alias);
                break;
        }

        return true;
    }


    private void sendHelpMessage(Player player, String alias) {
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━ TEAM COMMANDS ━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
        player.sendMessage(Component.text("/" + alias + " invite <Spieler>", NamedTextColor.GREEN)
                .append(Component.text(" - Lade einen Spieler zu deinem Team ein", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/" + alias + " accept <Spieler>", NamedTextColor.GREEN)
                .append(Component.text(" - Akzeptiere eine Team-Einladung", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/" + alias + " leave", NamedTextColor.GREEN)
                .append(Component.text(" - Verlasse dein aktuelles Team", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/" + alias + " kick", NamedTextColor.GREEN)
                .append(Component.text(" - Kicke ein Teammitglied aus deinem Team", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/" + alias + " list", NamedTextColor.GREEN)
                .append(Component.text(" - Zeige alle Teammitglieder an", NamedTextColor.GRAY)));
    }


    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String sub : SUB_COMMANDS) {
                if (sub.toLowerCase().startsWith(input)) {
                    completions.add(sub);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            String input = args[1].toLowerCase();

            if (args[0].equalsIgnoreCase("invite")) {
                List<String> playerNames = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.equals(player) && p.getName().toLowerCase().startsWith(input)) {
                        playerNames.add(p.getName());
                    }
                }
                return playerNames;
            }

            if (args[0].equalsIgnoreCase("accept")) {
                List<String> invites = TeamHandler.getPendingInvites().getOrDefault(player.getUniqueId(), new ArrayList<>());
                List<String> completions = new ArrayList<>();
                for (String name : invites) {
                    if (name.toLowerCase().startsWith(input)) {
                        completions.add(name);
                    }
                }
                return completions;
            }

            if (args[0].equalsIgnoreCase("kick")) {
                UUID playerUUID = player.getUniqueId();
                Integer teamId =  Config.getPlayerTeams().get(playerUUID);
                List<String> completions = new ArrayList<>();

                if(player.isOp()) {
                    for (UUID uuid : Config.getPlayerTeams().keySet()) {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
                        if (target.getName() != null) {
                            completions.add(target.getName());
                        }
                    }
                    return completions;
                }

                if (teamId != null && Config.getTeamLeaders().get(teamId).equals(playerUUID)) {
                    Set<UUID> teamMembers = Config.getTeams().get(teamId);
                    for (UUID memberUUID : teamMembers) {
                        if (!memberUUID.equals(playerUUID)) {
                            Player member = Bukkit.getPlayer(memberUUID);
                            if (member != null && member.getName().toLowerCase().startsWith(input)) {
                                completions.add(member.getName());
                            }
                        }
                    }
                    return completions;
                }
            }
        }
        return new ArrayList<>();
    }
}