package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockUpdateListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().isOp()) return;

        Block block = event.getBlock();
        if (block.getType() == Material.SPAWNER || isProtected(block.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {

        if (Config.isEventIsRunning()) return;

        event.setCancelled(true);
        event.blockList().removeIf(b -> isProtected(b.getLocation()));
    }

    private boolean isProtected(Location loc) {
        World w = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        for (Location spawn : Config.getPlayerSpawnLocations()) {
            if (spawn.getWorld() != w) continue;
            if (spawn.getBlockX() == x && spawn.getBlockY() - 1 == y && spawn.getBlockZ() == z) {
                return true;
            }
        }
        return false;
    }
}
