package de.einfachesache.flareonEvents.listener;

import de.einfachesache.flareonEvents.Config;
import de.einfachesache.flareonEvents.EventState;
import de.einfachesache.flareonEvents.GameHandler;
import de.einfachesache.flareonEvents.item.EventInfoBook;
import de.einfachesache.flareonEvents.item.Recipe;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(Component.text("§7" + player.getName() + " ist dem server beigetreten"));

        Recipe.discoverRecipe(player);
        GameHandler.resetPlayer(player, Config.getEventState().getId() <= 2, Config.getEventState().getId() <= 1);

        if (Config.isEventIsRunning()) {
            return;
        }

        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().setItem(8, EventInfoBook.createEventInfoBook());
        player.teleport(Config.getEventState() == EventState.STARTING ? GameHandler.getPlayerAssignedSpawn(player) : Config.getMainSpawnLocation());
    }
}
