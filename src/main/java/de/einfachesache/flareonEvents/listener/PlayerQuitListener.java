package de.einfachesache.flareonEvents.listener;

import de.einfachesache.flareonEvents.item.EventInfoBook;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void quitListener(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        player.getInventory().remove(EventInfoBook.createEventInfoBook());

        event.quitMessage(Component.text("§7" + player.getName() + " hat den Server verlassen"));
    }
}
