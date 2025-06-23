package de.einfachesache.flareonEvents.listener;

import de.einfachesache.flareonEvents.FlareonEvents;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    FlareonEvents plugin = FlareonEvents.getPlugin();

    @EventHandler
    public void deathListener(PlayerDeathEvent event) {
        event.setDeathMessage("§k22 §c§lAUSLÖSCHUNG! §fEin Spieler ist gestorben §k22");
        event.getPlayer().getWorld().spawnEntity(event.getPlayer().getLocation().add(0, 3,0), EntityType.LIGHTNING_BOLT);
    }
}
