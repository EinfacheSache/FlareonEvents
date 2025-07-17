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

        if (player.isOp()) return;
        if (Config.getEventState() == EventState.NOT_RUNNING) return;

        boolean isParticipant = Config.getParticipantsUUID().contains(uuid);
        boolean isDead = Config.getDeathParticipantsUUID().contains(uuid);

        if (isParticipant && !isDead) return;

        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text(
                " \n" +
                        (isDead ?
                                "§4§kAA §4§lAUSLÖSCHUNG! §kAA\n§cDu bist gestorben!" :
                                "§cDu bist dieses Mal leider nicht dabei!\nSpiele nächstes Mal mit :)")));
    }
}
