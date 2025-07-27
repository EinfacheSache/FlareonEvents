package de.einfachesache.flareonEvents;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeamHandler {

    static final FlareonEvents PLUGIN = FlareonEvents.getPlugin();
    static final int MAX_TEAM_SIZE = 3;
    static int nextTeamId = 1;


    // Speichert Team-Einladungen: Eingeladener Spieler -> Liste der Einladenden Spieler (Name)
    static final Map<UUID, List<String>> PENDING_INVITES = new ConcurrentHashMap<>();
    // Speichert Teams: Team-ID -> Liste der Teammitglieder (UUIDs)
    static final Map<Integer, Set<UUID>> TEAMS = new ConcurrentHashMap<>();
    // Speichert zu welchem Team ein Spieler gehört: Spieler -> Team-ID
    static final Map<UUID, Integer> PLAYER_TEAMS = new ConcurrentHashMap<>();
    // Speichert welcher Spieler welches Team leitet: Team-ID -> Leader-UUID
    static final Map<Integer, UUID> TEAM_LEADERS = new ConcurrentHashMap<>();


    public static void handleInviteCommand(Player sender, String[] args) {
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

        if (PLAYER_TEAMS.containsKey(targetUUID)) {
            sender.sendMessage(Component.text(targetName + " ist bereits in einem Team!", NamedTextColor.RED));
            return;
        }

        Integer inviterTeamId = PLAYER_TEAMS.get(sender.getUniqueId());
        if (inviterTeamId != null) {
            Set<UUID> team = TEAMS.get(inviterTeamId);
            if (team.size() >= MAX_TEAM_SIZE) {
                sender.sendMessage(Component.text("Dein Team ist bereits voll! (Max: " + MAX_TEAM_SIZE + ")", NamedTextColor.RED));
                return;
            }
        }

        List<String> invites = PENDING_INVITES.getOrDefault(targetUUID, new ArrayList<>());
        if (invites.contains(sender.getName())) {
            sender.sendMessage(Component.text(targetName + " hat bereits eine Einladung von dir!", NamedTextColor.RED));
            return;
        }

        PENDING_INVITES.computeIfAbsent(targetUUID, k -> new ArrayList<>()).add(sender.getName().toLowerCase());

        sender.sendMessage(Component.text("Team-Einladung an " + targetName + " gesendet!", NamedTextColor.GREEN));

        target.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.YELLOW));
        target.sendMessage(Component.text("TEAM EINLADUNG", NamedTextColor.GOLD));
        target.sendMessage(Component.text(sender.getName(), NamedTextColor.WHITE)
                .append(Component.text(" hat dich zu seinem Team eingeladen!", NamedTextColor.YELLOW)));

        Component acceptMessage = Component.text("[ANNEHMEN]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/team accept " + sender.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("Klicke um die Einladung anzunehmen", NamedTextColor.GRAY)));

        target.sendMessage(Component.text("→ ", NamedTextColor.YELLOW).append(acceptMessage)
                .append(Component.text(" oder verwende ", NamedTextColor.YELLOW))
                .append(Component.text("/team accept " + sender.getName(), NamedTextColor.WHITE)));

        target.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.YELLOW));

        Bukkit.getScheduler().runTaskLater(PLUGIN, () -> {
            List<String> currentInvites = PENDING_INVITES.get(targetUUID);
            if (currentInvites != null && currentInvites.remove(sender.getName())) {
                if (currentInvites.isEmpty()) {
                    PENDING_INVITES.remove(targetUUID);
                }
                if (target.isOnline()) {
                    target.sendMessage(Component.text("Die Team-Einladung von " + sender.getName() + " ist abgelaufen!", NamedTextColor.RED));
                }
                if (sender.isOnline()) {
                    sender.sendMessage(Component.text("Deine Team-Einladung an " + targetName + " ist abgelaufen!", NamedTextColor.RED));
                }
            }
        }, 1200L);
    }

    public static void handleAcceptCommand(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(Component.text("Verwendung: /team accept <Spielername>", NamedTextColor.RED));
            return;
        }

        UUID playerUUID = player.getUniqueId();

        if (!PENDING_INVITES.containsKey(playerUUID)) {
            player.sendMessage(Component.text("Du hast keine ausstehenden Team-Einladungen!", NamedTextColor.RED));
            return;
        }

        String inviterName = args[1];
        List<String> invites = PENDING_INVITES.get(playerUUID);
        if (!invites.contains(inviterName.toLowerCase())) {
            player.sendMessage(Component.text("Du hast keine Einladung von " + inviterName + "!", NamedTextColor.RED));
            return;
        }

        Player inviter = Bukkit.getPlayer(inviterName);
        if (inviter == null) {
            player.sendMessage(Component.text("Der Spieler " + inviterName + " ist nicht mehr online!", NamedTextColor.RED));
            invites.remove(inviterName.toLowerCase());
            if (invites.isEmpty()) {
                PENDING_INVITES.remove(playerUUID);
            }
            return;
        }

        UUID inviterUUID = inviter.getUniqueId();
        PENDING_INVITES.remove(playerUUID);

        Integer teamId = PLAYER_TEAMS.get(inviterUUID);
        if (teamId == null) {
            teamId = nextTeamId++;
            TEAMS.put(teamId, new HashSet<>());
            TEAMS.get(teamId).add(inviterUUID);
            PLAYER_TEAMS.put(inviterUUID, teamId);
            TEAM_LEADERS.put(teamId, inviterUUID);
        } else {
            Set<UUID> team = TEAMS.get(teamId);
            if (team.size() >= MAX_TEAM_SIZE) {
                player.sendMessage(Component.text("Das Team von " + inviter.getName() + " ist bereits voll!", NamedTextColor.RED));
                return;
            }
        }

        TEAMS.get(teamId).add(playerUUID);
        PLAYER_TEAMS.put(playerUUID, teamId);

        player.sendMessage(Component.text("Du bist dem Team #" + teamId + " von " + inviter.getName() + " beigetreten!", NamedTextColor.GREEN));
        inviter.sendMessage(Component.text(player.getName() + " ist deinem Team #" + teamId + " beigetreten!", NamedTextColor.GREEN));

        Set<UUID> teamMembers = TEAMS.get(teamId);
        for (UUID memberUUID : teamMembers) {
            if (!memberUUID.equals(playerUUID) && !memberUUID.equals(inviterUUID)) {
                Player member = Bukkit.getPlayer(memberUUID);
                if (member != null) {
                    member.sendMessage(Component.text(player.getName() + " ist dem Team #" + teamId + " beigetreten!", NamedTextColor.YELLOW));
                }
            }
        }
    }

    public static void handleLeaveCommand(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!PLAYER_TEAMS.containsKey(playerUUID)) {
            player.sendMessage(Component.text("Du bist in keinem Team!", NamedTextColor.RED));
            return;
        }

        Integer teamId = PLAYER_TEAMS.get(playerUUID);
        Set<UUID> teamMembers = TEAMS.get(teamId);

        // Spieler aus dem Team entfernen
        teamMembers.remove(playerUUID);
        PLAYER_TEAMS.remove(playerUUID);

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
            TEAMS.remove(teamId);
            TEAM_LEADERS.remove(teamId);
        }
        // Wenn der Leader das Team verlässt, neuen Leader bestimmen
        else if (TEAM_LEADERS.get(teamId).equals(playerUUID)) {
            UUID newLeader = teamMembers.iterator().next();
            TEAM_LEADERS.put(teamId, newLeader);
            Player newLeaderPlayer = Bukkit.getPlayer(newLeader);
            if (newLeaderPlayer != null) {
                newLeaderPlayer.sendMessage(Component.text("Du bist jetzt der neue Leader von Team #" + teamId + "!", NamedTextColor.GOLD));
            }
        }
    }

    public static void handleKickCommand(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(Component.text("Verwendung: /team kick <Spielername>", NamedTextColor.RED));
            return;
        }

        UUID playerUUID = player.getUniqueId();
        if (!PLAYER_TEAMS.containsKey(playerUUID)) {
            player.sendMessage(Component.text("Du bist in keinem Team!", NamedTextColor.RED));
            return;
        }

        Integer teamId = PLAYER_TEAMS.get(playerUUID);
        if (!TEAM_LEADERS.get(teamId).equals(playerUUID)) {
            player.sendMessage(Component.text("Nur der Team-Leader kann Spieler kicken!", NamedTextColor.RED));
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || !PLAYER_TEAMS.containsKey(target.getUniqueId()) || !PLAYER_TEAMS.get(target.getUniqueId()).equals(teamId)) {
            player.sendMessage(Component.text("Der Spieler ist nicht in deinem Team!", NamedTextColor.RED));
            return;
        }

        UUID targetUUID = target.getUniqueId();
        if (targetUUID.equals(playerUUID)) {
            player.sendMessage(Component.text("Du kannst dich nicht selbst kicken!", NamedTextColor.RED));
            return;
        }

        TEAMS.get(teamId).remove(targetUUID);
        PLAYER_TEAMS.remove(targetUUID);

        player.sendMessage(Component.text("Du hast " + target.getName() + " aus deinem Team entfernt!", NamedTextColor.YELLOW));
        target.sendMessage(Component.text("Du wurdest von " + player.getName() + " aus dem Team entfernt!", NamedTextColor.RED));
    }


    public static void handleListCommand(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!PLAYER_TEAMS.containsKey(playerUUID)) {
            player.sendMessage(Component.text("Du bist in keinem Team!", NamedTextColor.RED));
            return;
        }

        Integer teamId = PLAYER_TEAMS.get(playerUUID);
        Set<UUID> teamMembers = TEAMS.get(teamId);
        UUID leaderUUID = TEAM_LEADERS.get(teamId);

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

    public static Map<Integer, Set<UUID>> getTeams() {
        return TEAMS;
    }

    public static Map<UUID, Integer> getPlayerTeams() {
        return PLAYER_TEAMS;
    }

    public static Map<UUID, List<String>> getPendingInvites() {
        return PENDING_INVITES;
    }

    public static Map<Integer, UUID> getTeamLeaders() {
        return TEAM_LEADERS;
    }
}
