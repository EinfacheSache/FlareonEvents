package de.einfachesache.flareonEvents.listener;

import de.einfachesache.flareonEvents.Config;
import de.einfachesache.flareonEvents.EventState;
import de.einfachesache.flareonEvents.command.EventCommand;
import de.einfachesache.flareonEvents.item.EventInfoBook;
import de.einfachesache.flareonEvents.item.Recipe;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void joinListener(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(Component.text("§7" + player.getName() + " ist dem server beigetreten"));

        if(Config.getEventState() != EventState.RUNNING) {
            Recipe.discoverRecipe(player);
        }

        EventCommand.resetPlayer(player, Config.getEventState() != EventState.STARTING, Config.getEventState() != EventState.STARTING );

        if(Config.getEventState().getId() < 3) {
            player.teleport(Config.getMainSpawnLocation());
            player.getInventory().setItem(8, EventInfoBook.createEventInfoBook());
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 255));
        }
    }
}
