package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.handler.GameHandler;
import net.kyori.adventure.text.Component;
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
            player.setHealth(0);
            player.kick(Component.text("You are not allowed to respawn in this game."));
            return;
        }

        if (reason == PlayerRespawnEvent.RespawnReason.PLUGIN) {
            GameHandler.resetPlayer(player, true, true);
        }
    }
}
