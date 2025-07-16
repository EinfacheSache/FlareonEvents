package de.einfachesache.flareonEvents.listener;

import de.einfachesache.flareonEvents.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class PlayerFoodListener implements Listener {

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!Config.isEventIsRunning()) {
            event.setCancelled(true);
        }
    }
}
