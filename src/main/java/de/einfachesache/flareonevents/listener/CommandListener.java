package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.FlareonEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.HashSet;
import java.util.Set;

public class CommandListener implements Listener {

    private static final Set<String> BASE_COMMAND_BLACKLIST = Set.of("op", "deop", "stop", "reload", "restart", "plugins", "worldborder");
    private static final Set<String> BASE_COMMAND_WHITELIST = Set.of("help", "team", "recipe", "report", "msg", "reply");
    private static final Set<String> COMMAND_BLACKLIST = new HashSet<>();
    private static final Set<String> COMMAND_WHITELIST = new HashSet<>();

    public CommandListener() {
        for (String base : BASE_COMMAND_WHITELIST) {
            Command pluginCommand = FlareonEvents.getPlugin().getCommand(base);
            if (pluginCommand == null) {
                pluginCommand = Bukkit.getCommandMap().getCommand(base);
            }
            if (pluginCommand != null) {
                COMMAND_WHITELIST.add(base.toLowerCase());
                for (String alias : pluginCommand.getAliases()) {
                    COMMAND_WHITELIST.add(alias.toLowerCase());
                }
            }
        }

        for (String base : BASE_COMMAND_BLACKLIST) {
            Command pluginCommand = FlareonEvents.getPlugin().getCommand(base);
            if (pluginCommand == null) {
                pluginCommand = Bukkit.getCommandMap().getCommand(base);
            }
            if (pluginCommand != null) {
                COMMAND_BLACKLIST.add(base.toLowerCase());
                for (String alias : pluginCommand.getAliases()) {
                    COMMAND_BLACKLIST.add(alias.toLowerCase());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String cmdName = event.getMessage().split(" ")[0].substring(1).toLowerCase();
        Command cmd = Bukkit.getServer().getCommandMap().getCommand(cmdName);

        if (FlareonEvents.DEV_UUID.equals(player.getUniqueId())) {
            return;
        }

        if (cmd == null || !cmd.testPermissionSilent(player) || (!player.isOp() && !COMMAND_WHITELIST.contains(cmdName))) {
            event.setCancelled(true);
            player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du kannst diesen Befehl nicht verwenden.", NamedTextColor.RED)));
            return;
        }

        if (COMMAND_BLACKLIST.contains(cmdName)) {
            event.setCancelled(true);
            player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Dieser Befehl ist deaktiviert.", NamedTextColor.RED)));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();

        if (FlareonEvents.DEV_UUID.equals(player.getUniqueId())) {
            return;
        }

        event.getCommands().removeIf(command -> COMMAND_BLACKLIST.contains(command.toLowerCase()));

        if (player.isOp() || player.hasPermission("*")) {
            return;
        }

        event.getCommands().removeIf(command -> !COMMAND_WHITELIST.contains(command.toLowerCase()));
    }
}
