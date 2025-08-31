package de.einfachesache.flareonevents.command;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.FlareonEvents;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class BugReportCommand implements CommandExecutor, TabCompleter {

    private static final List<String> BUG_TYPES = Arrays.asList("ITEM", "PLAYER", "COMMAND", "GUI", "CHAT", "PERMISSION", "EVENT", "OTHER");
    private static final int COOLDOWN_TIME_SEC = 60;

    private final Map<UUID, Long> commandCooldown = new HashMap<>();
    private final Gson gson = new Gson();
    private final FlareonEvents plugin;

    public BugReportCommand(FlareonEvents plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDieser Command kann nur von Spielern verwendet werden!");
            return true;
        }

        if (args.length <= 1) {
            player.sendMessage("§7Nutzung: §e/" + alias + " <type> <kurze Beschreibung>");
            return true;
        }

        String bugType = args[0].toUpperCase(Locale.ROOT);
        if (!BUG_TYPES.contains(bugType)) {
            player.sendMessage("§7Ungültiger BugType. Bitte verwenden: " + BUG_TYPES);
            return true;
        }

        long timeLeft = (commandCooldown.getOrDefault(player.getUniqueId(), 0L) + COOLDOWN_TIME_SEC * 1000) - System.currentTimeMillis();
        if (timeLeft > 0) {
            player.sendMessage("§eBitte warte noch " + timeLeft / 1000 + "s bevor du einen neuen Report erstellst");
            return true;
        }
        commandCooldown.put(player.getUniqueId(), System.currentTimeMillis());

        var payload = new TicketPayload();
        payload.category = bugType;
        payload.message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        payload.reporter = new TicketPayload.Reporter();
        payload.reporter.uuid = player.getUniqueId().toString();
        payload.reporter.name = player.getName();

        var loc = player.getLocation();
        var ctx = new TicketPayload.Context();
        ctx.world = loc.getWorld().getName();
        ctx.x = loc.getX();
        ctx.y = loc.getY();
        ctx.z = loc.getZ();
        ctx.gamemode = player.getGameMode().name();

        ItemStack held = player.getInventory().getItemInMainHand();
        ctx.heldItem = held.getItemMeta() != null ? held.getItemMeta().getDisplayName() : held.getType().name();

        ctx.phase = Config.getEventState().getName();
        ctx.team = Config.getPlayerTeams().getOrDefault(player.getUniqueId(), null);
        try {
            ctx.client = player.getClientBrandName();
        } catch (Throwable ignored) {
            ctx.client = null;
        }

        payload.context = ctx;

        var out = ByteStreams.newDataOutput();
        out.writeUTF(gson.toJson(payload));
        player.sendPluginMessage(plugin, FlareonEvents.CH_TICKET, out.toByteArray());

        player.sendMessage("§aDanke! Dein Bug-Report wurde gesendet.");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) return BUG_TYPES;

        if (args.length == 1) {
            String prefix = args[0];
            if (prefix.isEmpty()) return BUG_TYPES;
            String p = prefix.toUpperCase(Locale.ROOT);
            return BUG_TYPES.stream()
                    .filter(t -> t.startsWith(p))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public static final class TicketPayload {
        public String category;
        public String message;
        public Reporter reporter;
        public Context context;

        public static final class Reporter {
            public String uuid;
            public String name;
        }

        public static final class Context {
            public String world;
            public double x, y, z;
            public String gamemode, heldItem;
            public Integer team;
            public String phase, client;
        }
    }
}