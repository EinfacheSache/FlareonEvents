package de.einfachesache.flareonevents.handler;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.FlareonEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
        getSpectatorsOf(e.getPlayer()).forEach((player) -> attach(player, true));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        List<Player> spectators = getSpectatorsOf(e.getPlayer());
        spectators.forEach(spectator -> {
            spectator.setSpectatorTarget(null);
            attach(spectator, true);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onStopSpectate(PlayerStopSpectatingEntityEvent e) {
        Player player = e.getPlayer();

        if (player.isOp()) {
            player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("§eDu hast das Zuschauen von §c" + e.getSpectatorTarget().getName() + "§e beendet")));
            return;
        }

        e.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true)
    public void onStartSpectate(PlayerStartSpectatingEntityEvent e) {
        Entity target = e.getNewSpectatorTarget();
        Player player = e.getPlayer();

        if (!(target instanceof Player targetPlayer)) {
            return;
        }

        if (player.isOp()) {
            return;
        }

        targetPlayer.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("§eDir schaut nun §c" + player.getName() + "§e zu")));
    }

    public static void attach(Player player, boolean reattach) {

        if (reattach) player.setSpectatorTarget(null);

        Bukkit.getScheduler().runTask(FlareonEvents.getPlugin(), () -> {
            Player target = getTarget(player);
            Location playerLocation = player.getLocation();

            if (target == null || !target.isOnline()) {
                FlareonEvents.getLogManager().error("Can't find Spectator target for " + player.getName());
                return;
            }

            if (player.isDead()) {
                player.spigot().respawn();
            }

            if (player.getGameMode() != GameMode.SPECTATOR) {
                player.setGameMode(GameMode.SPECTATOR);
            }

            boolean sameWorld = playerLocation.getWorld().equals(target.getWorld());
            boolean near = sameWorld && playerLocation.distanceSquared(target.getLocation()) <= 120 * 120;
            long delay = near ? (reattach ? 3 : 7) : 15;

            player.teleportAsync(target.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN).thenRun(() ->
                    Bukkit.getScheduler().runTaskLater(FlareonEvents.getPlugin(), () -> player.setSpectatorTarget(target), delay));
            player.sendMessage(FlareonEvents.PLUGIN_PREFIX.append(Component.text("§eDu schaust nun §c" + target.getName() + "§e zu. (took: " + delay + "ms)")));
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
