package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.event.EventSound;
import de.einfachesache.flareonevents.handler.GameHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class PortalCreateListener implements Listener {

    private static boolean nether = false;

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        if (event.getEntity() instanceof Player && event.getEntity().isOp())
            return;

        if (!nether && event.getReason() == PortalCreateEvent.CreateReason.FIRE) {
            event.setCancelled(true);
        }
    }

    public static void setNether(boolean nether) {

        if (nether) {
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                player.playSound(EventSound.START_END.adventure());
                player.sendActionBar(Component.text("Erkunde die Nether Welt!", NamedTextColor.GREEN));
                player.showTitle(Title.title(Component.text("Der Nether ist nun geöffnet!", NamedTextColor.DARK_RED), Component.text(""), GameHandler.times));
            });
            Bukkit.broadcast(Component.text("§aDer Nether ist ab jetzt geöffnet. Der Zugang ist freigegeben. Viel Erfolg."));
        }

        PortalCreateListener.nether = nether;
    }

    public static boolean netherEnabled() {
        return nether;
    }
}
