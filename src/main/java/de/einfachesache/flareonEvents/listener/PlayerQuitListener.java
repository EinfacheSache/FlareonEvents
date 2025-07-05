package de.einfachesache.flareonEvents.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void quitListener(PlayerQuitEvent event) {
        event.quitMessage(Component.text("§7" + event.getPlayer().getName() + " hat den Server verlassen"));
    }
}
