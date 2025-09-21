package de.einfachesache.flareonevents.handler;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.voicechat.VoiceModPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeamHandler {

    static final FlareonEvents PLUGIN = FlareonEvents.getPlugin();
    static final Map<UUID, List<String>> PENDING_INVITES = new ConcurrentHashMap<>();

    public static void handleInvite(Player sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Verwendung: /team invite <Spielername>", NamedTextColor.RED)));
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Spieler '" + targetName + "' ist nicht online!", NamedTextColor.RED)));
            return;
        }

        if (target.equals(sender)) {
            sender.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du kannst dich nicht selbst zu deinem Team einladen!", NamedTextColor.RED)));
            return;
        }

        UUID targetUUID = target.getUniqueId();

        if (Config.getPlayerTeams().containsKey(targetUUID)) {
            sender.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text(targetName + " ist bereits in einem Team!", NamedTextColor.RED)));
            return;
        }

        Integer inviterTeamId = Config.getPlayerTeams().get(sender.getUniqueId());
        if (inviterTeamId != null) {
            Set<UUID> team = Config.getTeams().get(inviterTeamId);
            if (team.size() >= Config.getMaxTeamSize()) {
                sender.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Dein Team ist bereits voll! (Max: " + Config.getMaxTeamSize() + ")", NamedTextColor.RED)));
                return;
            }
        }

        if (sender.getLocation().distance(target.getLocation()) > Config.getMaxInviteDistanz()) {
            sender.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du darfst maximal " + Config.getMaxInviteDistanz() + " Blöcke vom Spieler entfernt stehen, um ihm eine Einladung senden zu können!", NamedTextColor.RED)));
            return;
        }

        List<String> invites = PENDING_INVITES.getOrDefault(targetUUID, new ArrayList<>());
        if (invites.contains(sender.getName())) {
            sender.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text(targetName + " hat bereits eine Einladung von dir!", NamedTextColor.RED)));
            return;
        }

        PENDING_INVITES.computeIfAbsent(targetUUID, k -> new ArrayList<>()).add(sender.getName().toLowerCase());

        sender.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Team-Einladung an " + targetName + " gesendet!", NamedTextColor.GREEN)));

        target.sendMessage(Component.text("━━━━━━━━━━━━━━━ TEAM EINLADUNG ━━━━━━━━━━━━━━━ ", NamedTextColor.GOLD));
        target.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(sender.displayName()).append(Component.text(" hat dich zu seinem Team eingeladen!", NamedTextColor.YELLOW)));
        target.sendMessage(Component.text("→ ", NamedTextColor.YELLOW)
                .append(Component.text("[ANNEHMEN]", NamedTextColor.GREEN, TextDecoration.BOLD))
                .append(Component.text(" oder verwende ", NamedTextColor.YELLOW))
                .append(Component.text("/team accept " + sender.getName(), NamedTextColor.GREEN))
                .clickEvent(ClickEvent.runCommand("/team accept " + sender.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("Klicke um die Einladung anzunehmen", NamedTextColor.GRAY))));
        target.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));

        Bukkit.getScheduler().runTaskLater(PLUGIN, () -> {
            List<String> currentInvites = PENDING_INVITES.get(targetUUID);
            if (currentInvites != null && currentInvites.remove(sender.getName())) {
                if (currentInvites.isEmpty()) {
                    PENDING_INVITES.remove(targetUUID);
                }
                if (target.isOnline()) {
                    target.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Die Team-Einladung von " + sender.getName() + " ist abgelaufen!", NamedTextColor.RED)));
                }
                if (sender.isOnline()) {
                    sender.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Deine Team-Einladung an " + targetName + " ist abgelaufen!", NamedTextColor.RED)));
                }
            }
        }, 60 * 20L);
    }

    public static void handleAccept(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Verwendung: /team accept <Spielername>", NamedTextColor.RED)));
            return;
        }

        UUID playerUUID = player.getUniqueId();

        if (!PENDING_INVITES.containsKey(playerUUID)) {
            player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du hast keine ausstehenden Team-Einladungen!", NamedTextColor.RED)));
            return;
        }

        String inviterName = args[1];
        List<String> invites = PENDING_INVITES.get(playerUUID);
        if (!invites.contains(inviterName.toLowerCase())) {
            player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du hast keine Einladung von " + inviterName + "!", NamedTextColor.RED)));
            return;
        }

        Player inviter = Bukkit.getPlayer(inviterName);
        if (inviter == null) {
            player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Der Spieler " + inviterName + " ist nicht mehr online!", NamedTextColor.RED)));
            invites.remove(inviterName.toLowerCase());
            if (invites.isEmpty()) {
                PENDING_INVITES.remove(playerUUID);
            }
            return;
        }

        UUID inviterUUID = inviter.getUniqueId();

        Integer teamId = Config.getPlayerTeams().get(inviterUUID);
        if (teamId == null) {
            teamId = Config.getNextTeamId();
            Config.addTeam(teamId, inviterUUID);
            Config.setTeamLeader(teamId, inviterUUID);

            VoiceModPlugin.createGroup(teamId);
            VoiceModPlugin.addPlayerToTeam(inviterUUID, teamId);

        } else {
            Set<UUID> team = Config.getTeams().get(teamId);
            if (team.size() >= Config.getMaxTeamSize()) {
                player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Das Team von " + inviter.getName() + " ist bereits voll!", NamedTextColor.RED)));
                return;
            }
        }

        PENDING_INVITES.remove(playerUUID);
        Config.addPlayerToTeam(playerUUID, teamId);

        VoiceModPlugin.addPlayerToTeam(playerUUID, teamId);

        player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du bist dem Team #" + teamId + " von " + inviter.getName() + " beigetreten!", NamedTextColor.GREEN)));
        inviter.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text(player.getName() + " ist deinem Team #" + teamId + " beigetreten!", NamedTextColor.GREEN)));

        Set<UUID> teamMembers = Config.getTeams().get(teamId);
        for (UUID memberUUID : teamMembers) {
            if (!memberUUID.equals(playerUUID) && !memberUUID.equals(inviterUUID)) {
                Player member = Bukkit.getPlayer(memberUUID);
                if (member != null) {
                    member.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text(player.getName() + " ist dem Team #" + teamId + " beigetreten!", NamedTextColor.YELLOW)));
                }
            }
        }
    }

    public static void handleLeave(OfflinePlayer player, boolean isKick) {
        UUID playerUUID = player.getUniqueId();

        if (!Config.getPlayerTeams().containsKey(playerUUID)) {
            if(!isKick){
                Objects.requireNonNull(player.getPlayer()).sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du bist in keinem Team!", NamedTextColor.RED)));
            }
            return;
        }

        Integer teamId = Config.getPlayerTeams().get(playerUUID);
        Set<UUID> teamMembers = Config.getTeams().get(teamId);

        Config.removePlayerFromTeam(playerUUID);

        if (!isKick) {
            Objects.requireNonNull(player.getPlayer()).sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du hast das Team #" + teamId + " verlassen!", NamedTextColor.YELLOW)));
        }

        if (player.getPlayer() != null) {
            VoiceModPlugin.removePlayerToTeam(player.getPlayer().getUniqueId(), teamId);
        }

        for (UUID memberUUID : teamMembers) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                member.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text(player.getName() + " hat das Team #" + teamId + " verlassen!", NamedTextColor.YELLOW)));
            }
        }

        // Wenn der Leader das Team verlässt, neuen Leader bestimmen
        if (Config.getTeamLeaders().containsKey(teamId) && Config.getTeamLeaders().get(teamId).equals(playerUUID)) {
            UUID newLeader = teamMembers.iterator().next();
            Config.setTeamLeader(teamId, newLeader);
            Player newLeaderPlayer = Bukkit.getPlayer(newLeader);
            if (newLeaderPlayer != null) {
                newLeaderPlayer.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du bist jetzt der neue Leader von Team #" + teamId + "!", NamedTextColor.GOLD)));
            }
        }
    }

    public static void handleKick(Player player, String[] args) {

        if (args.length != 2) {
            player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Verwendung: /team kick <Spielername>", NamedTextColor.RED)));
            return;
        }

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(targetName);
        if (target == null) {
            player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Spieler nicht gefunden!", NamedTextColor.RED)));
            return;
        }

        UUID targetUUID = target.getUniqueId();
        Integer targetTeamId = Config.getPlayerTeams().get(targetUUID);

        if (targetTeamId == null) {
            player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Der Spieler ist in keinem Team!", NamedTextColor.RED)));
            return;
        }

        if (!player.isOp()) {
            UUID playerUUID = player.getUniqueId();

            if (!Config.getPlayerTeams().containsKey(playerUUID)) {
                player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du bist in keinem Team!", NamedTextColor.RED)));
                return;
            }

            Integer ownTeamId = Config.getPlayerTeams().get(playerUUID);
            if (!Config.getTeamLeaders().get(ownTeamId).equals(playerUUID)) {
                player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Nur der Team-Leader kann Spieler kicken!", NamedTextColor.RED)));
                return;
            }

            if (!ownTeamId.equals(targetTeamId)) {
                player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Der Spieler ist nicht in deinem Team!", NamedTextColor.RED)));
                return;
            }

            if (targetUUID.equals(playerUUID)) {
                player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du kannst dich nicht selbst kicken!", NamedTextColor.RED)));
                return;
            }
        }

        player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du hast " + target.getName() + " aus dem Team entfernt!", NamedTextColor.RED)));

        if (target.getPlayer() != null) {
            target.getPlayer().sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du wurdest von " + player.getName() + " aus dem Team entfernt!", NamedTextColor.RED)));
        }

        handleLeave(target, true);
    }


    public static void handleListCommand(Player player, String[] args) {
        UUID playerUUID = player.getUniqueId();
        Integer teamId;

        if (args.length == 2 && player.isOp()) {

            if (args[1].equalsIgnoreCase("all")) {
                Config.getPlayerTeams().values().forEach(id -> sendTeamList(player, id));
                return;
            }

            try {
                teamId = Integer.parseInt(args[1]);
                if (!Config.getTeams().containsKey(teamId)) {
                    player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Dieses Team existiert nicht!", NamedTextColor.RED)));
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Ungültige Team-ID!", NamedTextColor.RED)));
                return;
            }
        } else {
            if (!Config.getPlayerTeams().containsKey(playerUUID)) {
                player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("Du bist in keinem Team!", NamedTextColor.RED)));
                return;
            }
            teamId = Config.getPlayerTeams().get(playerUUID);
        }

        sendTeamList(player, teamId);

    }

    private static void sendTeamList(Player player, Integer teamId) {
        Set<UUID> teamMembers = Config.getTeams().get(teamId);
        UUID leaderUUID = Config.getTeamLeaders().get(teamId);

        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
        player.sendMessage(Component.text("TEAM #" + teamId + " (" + teamMembers.size() + " Mitglieder)", NamedTextColor.GOLD));

        for (UUID memberUUID : teamMembers) {
            OfflinePlayer member = Bukkit.getOfflinePlayer(memberUUID);
            String name = member.getName() != null ? member.getName() : "Unbekannt";
            boolean isOnline = member.isOnline();

            Component status = isOnline
                    ? Component.text("Online", NamedTextColor.GREEN)
                    : Component.text("Offline", NamedTextColor.RED);

            Component role = memberUUID.equals(leaderUUID)
                    ? Component.text(" (Leader)", NamedTextColor.GOLD)
                    : Component.empty();

            player.sendMessage(Component.text("• " + name + " - ", NamedTextColor.WHITE)
                    .append(status)
                    .append(role));
        }

        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
    }

    public static boolean arePlayersOnSameTeam(Player player1, Player player2) {
        int TeamIDPlayer1 = Config.getPlayerTeams().getOrDefault(player1.getUniqueId(), -1);
        int TeamIDPlayer2 = Config.getPlayerTeams().getOrDefault(player2.getUniqueId(), -2);
        return Objects.equals(TeamIDPlayer1, TeamIDPlayer2);
    }

    public static Map<UUID, List<String>> getPendingInvites() {
        return PENDING_INVITES;
    }
}
