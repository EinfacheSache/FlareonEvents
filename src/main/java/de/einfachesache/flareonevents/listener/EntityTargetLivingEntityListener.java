package de.einfachesache.flareonevents.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class EntityTargetLivingEntityListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTarget(EntityTargetLivingEntityEvent e) {
        if (!(e.getTarget() instanceof Player player)) return;
        if (player.getGameMode() == GameMode.ADVENTURE) {
            e.setTarget(null);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onGamemodeChange(PlayerGameModeChangeEvent e) {
        if (e.getNewGameMode() != GameMode.ADVENTURE) return;
        Player p = e.getPlayer();
        for (var ent : p.getWorld().getNearbyEntities(p.getLocation(), 48, 24, 48)) {
            if (ent instanceof Mob mob && mob.getTarget() == p) {
                mob.setTarget(null);
            }
        }
    }
}
