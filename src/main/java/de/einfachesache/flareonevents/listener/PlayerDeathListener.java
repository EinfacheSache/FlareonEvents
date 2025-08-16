package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.EventState;
import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.handler.GameHandler;
import de.einfachesache.flareonevents.handler.TeamHandler;
import de.einfachesache.flareonevents.item.misc.EventInfoBook;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDeathListener implements Listener {

    private static final Map<UUID, Integer> pvpKillCounts = new HashMap<>();
    NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "lightning_player_death");

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player deceased = event.getEntity();
        Player killer = deceased.getKiller();
        Location deathLocation = deceased.getLocation();

        AttributeInstance attr = deceased.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(20);
            deceased.setHealthScale(20);
        }

        if (Config.getEventState() != EventState.RUNNING) {
            event.getDrops().clear();
            new BukkitRunnable() {
                @Override
                public void run() {
                    deceased.spigot().respawn();
                    deceased.setGameMode(GameMode.ADVENTURE);
                    deceased.teleport(Config.getMainSpawnLocation());
                    deceased.getInventory().setItem(8, EventInfoBook.createEventInfoBook());
                }
            }.runTaskLater(FlareonEvents.getPlugin(), 1L);

            return;
        }


        if (Config.isKickOnDeath()) {
            deceased.kick(Component.text("§4§kAA §4§lAUSLÖSCHUNG! §kAA\n§cDu bist gestorben!"));
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    deceased.spigot().respawn();
                    deceased.teleport(deathLocation);
                    deceased.setGameMode(GameMode.SPECTATOR);
                }
            }.runTaskLater(FlareonEvents.getPlugin(), 1L);
        }

        if (killer != null && !killer.equals(deceased)) {
            UUID uuid = killer.getUniqueId();
            pvpKillCounts.put(uuid, pvpKillCounts.getOrDefault(uuid, 0) + 1);
        }

        event.deathMessage(Component.text("§k22 §c§lAUSLÖSCHUNG! §fEin Spieler ist gestorben §k22"));
        event.getDrops().add(SoulHeartCrystal.createSoulHeartCrystal());
        Config.addDeathParticipant(event.getPlayer().getUniqueId());
        deceased.getWorld().strikeLightning(deceased.getLocation().add(0, 2.5, 0))
                .getPersistentDataContainer().set(namespacedKey, PersistentDataType.BYTE, (byte) 1);

        TeamHandler.handleLeave(deceased, true);
        if (GameHandler.isEventEnd()) {
            GameHandler.endEvent();
        }
    }

    public static Map<UUID, Integer> getPvpKillCounts() {
        return pvpKillCounts;
    }
}
