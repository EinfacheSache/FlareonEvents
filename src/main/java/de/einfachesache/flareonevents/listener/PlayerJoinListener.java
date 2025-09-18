package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.event.EventState;
import de.einfachesache.flareonevents.handler.GameHandler;
import de.einfachesache.flareonevents.handler.TexturepackHandler;
import de.einfachesache.flareonevents.item.ItemRecipe;
import de.einfachesache.flareonevents.item.misc.EventInfoBook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        TexturepackHandler.setTexturepack(player);

        event.joinMessage(player.displayName().append(Component.text(" ist dem Server beigetreten", NamedTextColor.GRAY)));

        ItemRecipe.discoverRecipe(player);
        int stateId = Config.getEventState().getId();
        GameHandler.resetPlayer(player, stateId <= 2, stateId <= 1);

        if (!Config.isEventIsRunning()) {
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().setItem(8, EventInfoBook.create());
            player.teleportAsync(
                    Config.getEventState() == EventState.STARTING
                            ? GameHandler.getPlayerAssignedSpawn(player)
                            : Config.getMainSpawnLocation(),
                    PlayerTeleportEvent.TeleportCause.PLUGIN
            );
        }
    }
}
