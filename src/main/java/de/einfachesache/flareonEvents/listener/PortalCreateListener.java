package de.einfachesache.flareonEvents.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class PortalCreateListener implements Listener {

    private static boolean nether = false;

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        if(event.getEntity() instanceof Player && event.getEntity().isOp())
            return;

        if (!nether && event.getReason() == PortalCreateEvent.CreateReason.FIRE) {
            event.setCancelled(true);
        }
    }

    public static void setNether(boolean nether) {
        if(nether)
            Bukkit.broadcast(Component.text("§aDer Nether ist ab jetzt geöffnet. Der Zugang ist freigegeben. Viel Erfolg."));
        PortalCreateListener.nether = nether;
    }
}
