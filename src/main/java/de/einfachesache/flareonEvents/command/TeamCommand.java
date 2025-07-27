package de.einfachesache.flareonEvents.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeamCommand implements CommandExecutor, TabCompleter, Listener {


    private static final List<String> SUB_COMMANDS = Arrays.asList("invite", "accept", "leave", "list");

    // Speichert Team-Einladungen: Eingeladener Spieler -> Liste der Einladenden Spieler (Name)
    private final Map<UUID, List<String>> pendingInvites = new ConcurrentHashMap<>();

    // Speichert Teams: Team-ID -> Liste der Teammitglieder (UUIDs)
    private final Map<Integer, Set<UUID>> teams = new ConcurrentHashMap<>();

    // Speichert zu welchem Team ein Spieler gehört: Spieler -> Team-ID
    private final Map<UUID, Integer> playerTeams = new ConcurrentHashMap<>();

    // Speichert welcher Spieler welches Team leitet: Team-ID -> Leader-UUID
    private final Map<Integer, UUID> teamLeaders = new ConcurrentHashMap<>();


    // Max Team size
    private final int maxTeamSize = 3;

    // Counter für Team-IDs
    private int nextTeamId = 1;


    private final JavaPlugin plugin;

    public TeamCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Dieser Command kann nur von Spielern verwendet werden!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "invite":
                handleInviteCommand(player, args);
                break;
            case "accept":
                handleAcceptCommand(player, args);
                break;
            case "leave":
                handleLeaveCommand(player);
                break;
            case "list":
                handleListCommand(player);
                break;
            default:
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private void handleInviteCommand(Player sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Component.text("Verwendung: /team invite <Spielername>", NamedTextColor.RED));
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(Component.text("Spieler '" + targetName + "' ist nicht online!", NamedTextColor.RED));
            return;
        }

        if (target.equals(sender)) {
            sender.sendMessage(Component.text("Du kannst dich nicht selbst zu deinem Team einladen!", NamedTextColor.RED));
            return;
        }

        UUID targetUUID = target.getUniqueId();

        // Prüfen ob der Spieler bereits in einem Team ist
        if (playerTeams.containsKey(targetUUID)) {
            sender.sendMessage(Component.text(targetName + " ist bereits in einem Team!", NamedTextColor.RED));
            return;
        }

        // Prüfen ob bereits eine Einladung von diesem Spieler existiert
        List<String> invites = pendingInvites.getOrDefault(targetUUID, new ArrayList<>());
        if (invites.contains(sender.getName())) {
            sender.sendMessage(Component.text(targetName + " hat bereits eine Einladung von dir!", NamedTextColor.RED));
            return;
        }

        // Einladung hinzufügen
        pendingInvites.computeIfAbsent(targetUUID, k -> new ArrayList<>()).add(sender.getName().toLowerCase());

        // Nachrichten senden
        sender.sendMessage(Component.text("Team-Einladung an " + targetName + " gesendet!", NamedTextColor.GREEN));

        target.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.YELLOW));
        target.sendMessage(Component.text("TEAM EINLADUNG", NamedTextColor.GOLD));
        target.sendMessage(Component.text(sender.getName(), NamedTextColor.WHITE)
                .append(Component.text(" hat dich zu seinem Team eingeladen!", NamedTextColor.YELLOW)));

        // Klickbare Accept-Nachricht
        Component acceptMessage = Component.text("[ANNEHMEN]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/team accept " + sender.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("Klicke um die Einladung anzunehmen", NamedTextColor.GRAY)));

        target.sendMessage(Component.text("→ ", NamedTextColor.YELLOW).append(acceptMessage)
                .append(Component.text(" oder verwende ", NamedTextColor.YELLOW))
                .append(Component.text("/team accept " + sender.getName(), NamedTextColor.WHITE)));

        target.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.YELLOW));

        // Einladung nach 60 Sekunden automatisch ablaufen lassen
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            List<String> currentInvites = pendingInvites.get(targetUUID);
            if (currentInvites != null && currentInvites.remove(sender.getName())) {
                if (currentInvites.isEmpty()) {
                    pendingInvites.remove(targetUUID);
                }
                if (target.isOnline()) {
                    target.sendMessage(Component.text("Die Team-Einladung von " + sender.getName() + " ist abgelaufen!", NamedTextColor.RED));
                }
                if (sender.isOnline()) {
                    sender.sendMessage(Component.text("Deine Team-Einladung an " + targetName + " ist abgelaufen!", NamedTextColor.RED));
                }
            }
        }, 1200L); // 60 Sekunden = 1200 Ticks
    }

    private void handleAcceptCommand(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(Component.text("Verwendung: /team accept <Spielername>", NamedTextColor.RED));
            return;
        }

        UUID playerUUID = player.getUniqueId();

        if (!pendingInvites.containsKey(playerUUID)) {
            player.sendMessage(Component.text("Du hast keine ausstehenden Team-Einladungen!", NamedTextColor.RED));
            return;
        }

        String inviterName = args[1].toLowerCase();
        List<String> invites = pendingInvites.get(playerUUID);
        if (!invites.contains(inviterName)) {
            player.sendMessage(Component.text("Du hast keine Einladung von " + inviterName + "!", NamedTextColor.RED));
            return;
        }

        Player inviter = Bukkit.getPlayer(inviterName);
        if (inviter == null) {
            player.sendMessage(Component.text("Der Spieler " + inviterName + " ist nicht mehr online!", NamedTextColor.RED));
            // Einladung entfernen
            invites.remove(inviterName);
            if (invites.isEmpty()) {
                pendingInvites.remove(playerUUID);
            }
            return;
        }

        UUID inviterUUID = inviter.getUniqueId();

        // Alle Einladungen für den Spieler entfernen
        pendingInvites.remove(playerUUID);

        // Prüfen ob der Einlader bereits ein Team hat
        Integer teamId = playerTeams.get(inviterUUID);
        if (teamId == null) {
            // Neues Team erstellen
            teamId = nextTeamId++;
            teams.put(teamId, new HashSet<>());
            teams.get(teamId).add(inviterUUID);
            playerTeams.put(inviterUUID, teamId);
            teamLeaders.put(teamId, inviterUUID);
        }

        // Spieler zum Team hinzufügen
        teams.get(teamId).add(playerUUID);
        playerTeams.put(playerUUID, teamId);

        // Erfolg-Nachrichten
        player.sendMessage(Component.text("Du bist dem Team #" + teamId + " von " + inviter.getName() + " beigetreten!", NamedTextColor.GREEN));
        inviter.sendMessage(Component.text(player.getName() + " ist deinem Team #" + teamId + " beigetreten!", NamedTextColor.GREEN));

        // Alle Teammitglieder benachrichtigen
        Set<UUID> teamMembers = teams.get(teamId);
        for (UUID memberUUID : teamMembers) {
            if (!memberUUID.equals(playerUUID) && !memberUUID.equals(inviterUUID)) {
                Player member = Bukkit.getPlayer(memberUUID);
                if (member != null) {
                    member.sendMessage(Component.text(player.getName() + " ist dem Team #" + teamId + " beigetreten!", NamedTextColor.YELLOW));
                }
            }
        }
    }

    private void handleLeaveCommand(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!playerTeams.containsKey(playerUUID)) {
            player.sendMessage(Component.text("Du bist in keinem Team!", NamedTextColor.RED));
            return;
        }

        Integer teamId = playerTeams.get(playerUUID);
        Set<UUID> teamMembers = teams.get(teamId);

        // Spieler aus dem Team entfernen
        teamMembers.remove(playerUUID);
        playerTeams.remove(playerUUID);

        player.sendMessage(Component.text("Du hast das Team #" + teamId + " verlassen!", NamedTextColor.YELLOW));

        // Alle Teammitglieder benachrichtigen
        for (UUID memberUUID : teamMembers) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                member.sendMessage(Component.text(player.getName() + " hat das Team #" + teamId + " verlassen!", NamedTextColor.YELLOW));
            }
        }

        // Wenn das Team leer ist, entfernen
        if (teamMembers.isEmpty()) {
            teams.remove(teamId);
            teamLeaders.remove(teamId);
        }
        // Wenn der Leader das Team verlässt, neuen Leader bestimmen
        else if (teamLeaders.get(teamId).equals(playerUUID)) {
            UUID newLeader = teamMembers.iterator().next();
            teamLeaders.put(teamId, newLeader);
            Player newLeaderPlayer = Bukkit.getPlayer(newLeader);
            if (newLeaderPlayer != null) {
                newLeaderPlayer.sendMessage(Component.text("Du bist jetzt der neue Leader von Team #" + teamId + "!", NamedTextColor.GOLD));
            }
        }
    }

    private void handleListCommand(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!playerTeams.containsKey(playerUUID)) {
            player.sendMessage(Component.text("Du bist in keinem Team!", NamedTextColor.RED));
            return;
        }

        Integer teamId = playerTeams.get(playerUUID);
        Set<UUID> teamMembers = teams.get(teamId);
        UUID leaderUUID = teamLeaders.get(teamId);

        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
        player.sendMessage(Component.text("TEAM #" + teamId + " (" + teamMembers.size() + " Mitglieder)", NamedTextColor.GOLD));

        for (UUID memberUUID : teamMembers) {
            Player member = Bukkit.getPlayer(memberUUID);
            String name = member != null ? member.getName() : "Offline";
            Component status = member != null ? Component.text("Online", NamedTextColor.GREEN) : Component.text("Offline", NamedTextColor.RED);
            Component role = memberUUID.equals(leaderUUID) ? Component.text(" (Leader)", NamedTextColor.GOLD) : Component.empty();

            player.sendMessage(Component.text("• " + name + " - ", NamedTextColor.WHITE)
                    .append(status)
                    .append(role));
        }

        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
        player.sendMessage(Component.text("TEAM COMMANDS", NamedTextColor.GOLD));
        player.sendMessage(Component.text("/team invite <Spieler>", NamedTextColor.WHITE)
                .append(Component.text(" - Lade einen Spieler zu deinem Team ein", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/team accept <Spielername>", NamedTextColor.WHITE)
                .append(Component.text(" - Akzeptiere eine Team-Einladung", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/team leave", NamedTextColor.WHITE)
                .append(Component.text(" - Verlasse dein aktuelles Team", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/team list", NamedTextColor.WHITE)
                .append(Component.text(" - Zeige alle Teammitglieder an", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
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

        if (args.length == 2 && args[0].equalsIgnoreCase("invite")) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    playerNames.add(player.getName());
                }
            }
            return playerNames;
        }

        return new ArrayList<>();
    }

    // Getter für andere Klassen (falls benötigt)
    public Map<Integer, Set<UUID>> getTeams() {
        return teams;
    }

    public Map<UUID, Integer> getPlayerTeams() {
        return playerTeams;
    }

    public Map<UUID, List<String>> getPendingInvites() {
        return pendingInvites;
    }

    public Map<Integer, UUID> getTeamLeaders() {
        return teamLeaders;
    }

    public int getNextTeamId() {
        return nextTeamId;
    }
}