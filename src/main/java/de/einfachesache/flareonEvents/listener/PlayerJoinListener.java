package de.einfachesache.flareonEvents.listener;

import de.einfachesache.flareonEvents.Config;
import de.einfachesache.flareonEvents.EventState;
import de.einfachesache.flareonEvents.command.EventCommand;
import de.einfachesache.flareonEvents.item.Recipe;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void joinListener(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(Component.text("§7" + player.getName() + " ist dem server beigetreten"));

        if(!player.hasPlayedBefore()) {
            Recipe.discoverRecipe(player);
        }

        if(Config.getEventState() == EventState.PAUSED || Config.getEventState() == EventState.NOT_RUNNING){
            player.teleport(Config.getMainSpawnLocation());
        }

        EventCommand.resetPlayer(player, Config.getEventState() != EventState.STARTING, Config.getEventState() != EventState.STARTING );

        Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0.42);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.05f);

        if(player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR)
            player.setGameMode(GameMode.SURVIVAL);
    }
}
