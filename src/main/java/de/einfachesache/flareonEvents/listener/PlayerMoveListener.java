package de.einfachesache.flareonEvents.listener;

import de.einfachesache.flareonEvents.Config;
import de.einfachesache.flareonEvents.EventState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if(event.getPlayer().isOp())
            return;

        if(Config.getEventState() == EventState.STARTING && event.hasChangedPosition()){
            event.setCancelled(true);
        }
    }
}
