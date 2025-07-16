package de.einfachesache.flareonEvents.listener;

import de.einfachesache.flareonEvents.Config;
import de.einfachesache.flareonEvents.EventState;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener {

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {

        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (player.isOp()) {
            return;
        }

        if (Config.getEventState() == EventState.NOT_RUNNING)
            return;

        boolean isParticipant = Config.getParticipantsUUID().contains(uuid);
        boolean isDead = Config.getDeathParticipantsUUID().contains(uuid);

        if (isParticipant && !isDead) {
            return;
        }

        if (isDead) {
            player.kick(Component.text("§4§kAA §4§lAUSLÖSCHUNG! §kAA\n§cDu bist gestorben!"));
            return;
        }

        player.kick(Component.text("§cDu bist dieses Mal leider nicht dabei!\nSpiele nächstes Mal mit :)"));
    }
}
