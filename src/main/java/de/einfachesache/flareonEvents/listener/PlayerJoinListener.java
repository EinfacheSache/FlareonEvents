package de.einfachesache.flareonEvents.listener;

import de.einfachesache.flareonEvents.FlareonEvents;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    FlareonEvents plugin = FlareonEvents.getPlugin();

    @EventHandler
    public void joinListener(PlayerJoinEvent event) {
        event.setJoinMessage("§7" + event.getPlayer().getName() + " ist dem server beigetreten (" + plugin.getServer().getOnlinePlayers().size() + "/"+ plugin.getServer().getMaxPlayers() + ")");
    }
}
