package de.einfachesache.flareonEvents.listener;

import de.einfachesache.flareonEvents.Config;
import de.einfachesache.flareonEvents.EventState;
import de.einfachesache.flareonEvents.FlareonEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDeathListener implements Listener {

    NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "lightning_player_death");

    @EventHandler
    public void deathListener(PlayerDeathEvent event) {

        if(Config.getEventState() == EventState.RUNNING){
            Config.addDeathParticipant(event.getPlayer().getUniqueId());
            event.getPlayer().kick(Component.text("§4§kAA §4§lAUSLÖSCHUNG! §kAA\n§cDu bist gestorben!"));
        }else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Player player = event.getPlayer();
                    player.spigot().respawn();
                    player.setGameMode(GameMode.ADVENTURE);
                    player.teleport(Config.getMainSpawnLocation());
                }
            }.runTaskLater(FlareonEvents.getPlugin(), 2L);
        }

        event.deathMessage(Component.text( "§k22 §c§lAUSLÖSCHUNG! §fEin Spieler ist gestorben §k22"));
        event.getPlayer().getWorld().strikeLightning(event.getPlayer().getLocation().add(0,1.5,0))
                .getPersistentDataContainer().set(namespacedKey, PersistentDataType.BYTE, (byte) 1);
    }
}
