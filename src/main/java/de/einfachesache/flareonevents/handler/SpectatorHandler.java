package de.einfachesache.flareonevents.handler;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.FlareonEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class SpectatorHandler implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        getSpectatorsOf(e.getPlayer()).forEach(SpectatorHandler::attach);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        List<Player> players = getSpectatorsOf(e.getPlayer());
        players.forEach(player -> {
            player.setSpectatorTarget(null);
            attach(player);
        });
    }

    public static void attach(Player player) {
        if (player.getGameMode() != GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        player.setSpectatorTarget(null);

        Bukkit.getScheduler().runTask(FlareonEvents.getPlugin(), () -> {
            Player target = getTarget(player);
            if (target == null || !target.isOnline()) {
                FlareonEvents.getLogManager().error("Can't find Spectator target for " + player.getName());
                return;
            }

            boolean sameWorld = player.getWorld().equals(target.getWorld());
            boolean near = sameWorld && player.getLocation().distanceSquared(target.getLocation()) <= 90 * 90;

            if (near) {
                Bukkit.getScheduler().runTask(FlareonEvents.getPlugin(), () -> player.setSpectatorTarget(target));
            } else {
                long delay = sameWorld ? 8L : 12L;
                player.teleportAsync(target.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN).thenRun(() ->
                        Bukkit.getScheduler().runTaskLater(FlareonEvents.getPlugin(), () -> player.setSpectatorTarget(target), delay));
            }

            player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(
                    Component.text("§eDu schaust nun §c" + target.getName() + "§e zu.")
            ));
        });
    }

    private static Player getTarget(Player player) {
        Integer teamId = Config.getPlayerTeams().get(player.getUniqueId());
        Player killer = player.getKiller();
        Player target = killer;

        if (teamId != null) {
            target = Config.getTeams()
                    .getOrDefault(teamId, Collections.emptySet())
                    .stream()
                    .filter(id -> !id.equals(player.getUniqueId()))
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .filter(p -> p.getGameMode() == GameMode.SURVIVAL)
                    .findAny()
                    .orElse(killer);
        }

        if (target == null || !target.isOnline()) {
            List<Player> candidates = Bukkit.getOnlinePlayers().stream()
                    .map(Player.class::cast)
                    .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                    .filter(p -> p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE)
                    .toList();
            if (!candidates.isEmpty()) {
                target = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
            }
        }

        return target;
    }

    public static List<Player> getSpectatorsOf(Entity target) {
        List<Player> res = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR && target.equals(player.getSpectatorTarget())) {
                res.add(player);
            }
        }
        return res;
    }
}
