package de.einfachesache.flareonevents.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class EntityTargetLivingEntityListener implements Listener {

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent e) {
        if (!(e.getTarget() instanceof Player p)) return;
        if (p.getGameMode() == GameMode.ADVENTURE) {
            e.setTarget(null);
            e.setCancelled(true);
        }
    }
}
