package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.handler.GameHandler;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        PlayerRespawnEvent.RespawnReason reason = e.getRespawnReason();

        if (reason == PlayerRespawnEvent.RespawnReason.DEATH) {
            GameHandler.resetPlayer(player, true, true);
            player.setGameMode(GameMode.SPECTATOR);
            return;
        }

        if (reason == PlayerRespawnEvent.RespawnReason.PLUGIN) {
            GameHandler.resetPlayer(player, true, true);
        }
    }
}
