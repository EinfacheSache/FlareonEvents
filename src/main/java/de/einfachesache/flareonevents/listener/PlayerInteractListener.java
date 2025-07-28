package de.einfachesache.flareonevents.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onBlockInteraction(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (block.getType() == Material.RESPAWN_ANCHOR) {
            event.setUseInteractedBlock(Event.Result.DENY);
        }

        if (block.getType().name().contains("BED") && event.getPlayer().getWorld().getName().equalsIgnoreCase("world_nether")) {
            event.setUseInteractedBlock(Event.Result.DENY);
        }
    }
}
