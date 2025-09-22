package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.item.misc.EventInfoBook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        player.getInventory().remove(EventInfoBook.create());

        event.quitMessage(player.displayName().append(Component.text(" hat den Server verlassen", NamedTextColor.GRAY)));
    }
}
