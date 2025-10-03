package de.einfachesache.flareonevents.command;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.handler.TeamHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MsgCommand implements CommandExecutor, TabCompleter {

    private static final String TEAM_TOKEN = "@team";

    public static final Map<UUID, String> LAST_MESSAGED = new ConcurrentHashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Dieser Command kann nur von Spielern verwendet werden!", NamedTextColor.RED));
            return true;
        }

        String name = cmd.getName().toLowerCase(Locale.ROOT);

        switch (name) {
            case "msg" -> {
                if (args.length <= 1) {
                    player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Verwendung: /msg <Spieler|@team> <Nachricht>", NamedTextColor.RED)));
                    return true;
                }

                String targetArg = args[0];
                String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
                if (text.isEmpty()) {
                    player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Deine Nachricht ist leer.", NamedTextColor.RED)));
                    return true;
                }

                if (TEAM_TOKEN.equalsIgnoreCase(targetArg)) {
                    TeamHandler.handleTeamMsg(player, text);
                    return true;
                }

                Player target = Bukkit.getPlayerExact(targetArg);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Dieser Spieler ist nicht online.", NamedTextColor.RED)));
                    return true;
                }
                if (target.equals(player)) {
                    player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du kannst dir nicht selbst schreiben.", NamedTextColor.RED)));
                    return true;
                }

                handleMsg(player, target, text);
                return true;
            }

            case "reply" -> {
                if (args.length == 0) {
                    player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Verwendung: /reply <Nachricht>", NamedTextColor.RED)));
                    return true;
                }
                String text = String.join(" ", args).trim();
                if (text.isEmpty()) {
                    player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Deine Nachricht ist leer.", NamedTextColor.RED)));
                    return true;
                }
                handleReply(player, text);
                return true;
            }

            default -> {
                return false;
            }
        }
    }

    private void handleMsg(Player sender, Player target, String text) {
        sender.sendMessage(Component.text("→ ", NamedTextColor.GRAY)
                .append(Component.text(target.getName(), NamedTextColor.GOLD))
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(text, NamedTextColor.WHITE)));

        target.sendMessage(Component.text("← ", NamedTextColor.GRAY)
                .append(Component.text(sender.getName(), NamedTextColor.GOLD))
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(text, NamedTextColor.WHITE)));

        // Für /reply merken (beidseitig, damit beide direkt /reply nutzen können)
        LAST_MESSAGED.put(sender.getUniqueId(), target.getUniqueId().toString());
        LAST_MESSAGED.put(target.getUniqueId(), sender.getUniqueId().toString());
    }


    private void handleReply(Player sender, String text) {
        String last = LAST_MESSAGED.get(sender.getUniqueId());
        if (last == null) {
            sender.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Es gibt niemanden, dem du antworten kannst.", NamedTextColor.RED)));
            return;
        }

        if(last.equalsIgnoreCase("@team")) {
            TeamHandler.handleTeamMsg(sender, text);
            return;
        }

        Player target = Bukkit.getPlayer(UUID.fromString(last));
        if (target == null || !target.isOnline()) {
            sender.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Dieser Spieler ist nicht mehr online.", NamedTextColor.RED)));
            return;
        }
        if (target.equals(sender)) {
            sender.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du kannst dir nicht selbst schreiben.", NamedTextColor.RED)));
            return;
        }

        handleMsg(sender, target, text);
    }


    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("msg")) return Collections.emptyList();

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);

            List<String> suggestions = new ArrayList<>();

            if (TEAM_TOKEN.startsWith(input)) {
                suggestions.add(TEAM_TOKEN);
            }

            for (Player p : Bukkit.getOnlinePlayers()) {
                String name = p.getName();
                if (name.toLowerCase(Locale.ROOT).startsWith(input)) {
                    suggestions.add(name);
                }
            }
            return suggestions;
        }

        return Collections.emptyList();
    }
}
