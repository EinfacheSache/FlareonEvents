package de.einfachesache.flareonEvents.listener;

import de.einfachesache.flareonEvents.Config;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockUpdateListener implements Listener {

    private boolean isProtected(Location loc) {
        return Config.getPlayerSpawnLocations().stream().anyMatch (location -> location.getBlock().getLocation().subtract(0,1,0).equals(loc));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        if (isProtected(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(b -> isProtected(b.getLocation()));
    }
}
