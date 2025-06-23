package de.einfachesache.flareonEvents.listener;

import de.einfachesache.flareonEvents.FlareonEvents;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    FlareonEvents plugin = FlareonEvents.getPlugin();

    @EventHandler
    public void quitListener(PlayerQuitEvent event) {
        event.setQuitMessage("§7" + event.getPlayer().getName() + " hat den Server verlassen (" + (plugin.getServer().getOnlinePlayers().size()-1)+ "/"+  plugin.getServer().getMaxPlayers() + ")");
    }
}
