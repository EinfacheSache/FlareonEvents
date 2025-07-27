package de.einfachesache.flareonEvents.listener;

import de.einfachesache.flareonEvents.Config;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockUpdateListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isProtected(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {

        if(!Config.isEventIsRunning()){
            event.setCancelled(true);
        }

        event.blockList().removeIf(b -> isProtected(b.getLocation()));
    }

    private boolean isProtected(Location loc) {
        return Config.getPlayerSpawnLocations().stream().anyMatch(location -> location.getBlock().getLocation().subtract(0, 1, 0).equals(loc));
    }
}
