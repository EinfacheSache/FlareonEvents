package de.einfachesache.flareonevents.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        event.renderer((source, displayName, msg, viewer) ->
                Component.text()
                        .append(player.displayName())
                        .append(Component.text(" | ").color(NamedTextColor.GRAY))
                        .append(msg.color(player.isOp() ? NamedTextColor.WHITE : NamedTextColor.GRAY))
                        .build()
        );
    }
}
