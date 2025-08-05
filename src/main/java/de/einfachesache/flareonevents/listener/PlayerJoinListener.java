package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.EventState;
import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.handler.GameHandler;
import de.einfachesache.flareonevents.item.ItemRecipe;
import de.einfachesache.flareonevents.item.misc.EventInfoBook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.setResourcePack(
                "http://einfachesache.de/flareon/Flareon-Events-V1.zip",
                null,
                Component.text("§bBenötigtes Texturepack für FlareonEvents laden?"),
                !FlareonEvents.DEV_UUID.equals(player.getUniqueId()) && !player.isOp()
        );

        event.joinMessage(player.displayName().append(Component.text(" ist dem server beigetreten", NamedTextColor.GRAY)));

        ItemRecipe.discoverRecipe(player);
        GameHandler.resetPlayer(player, Config.getEventState().getId() <= 2, Config.getEventState().getId() <= 1);

        if (Config.isEventIsRunning()) {
            return;
        }

        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().setItem(8, EventInfoBook.createEventInfoBook());
        player.teleport(Config.getEventState() == EventState.STARTING ? GameHandler.getPlayerAssignedSpawn(player) : Config.getMainSpawnLocation());
    }
}
