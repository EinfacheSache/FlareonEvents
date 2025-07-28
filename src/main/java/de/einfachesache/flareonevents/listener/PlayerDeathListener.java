package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.EventState;
import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.ItemUtils;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
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

        if (Config.getEventState() == EventState.RUNNING) {
            Config.addDeathParticipant(event.getPlayer().getUniqueId());
            deceased.kick(Component.text("§4§kAA §4§lAUSLÖSCHUNG! §kAA\n§cDu bist gestorben!"));
        } else {
            event.getDrops().clear();
            new BukkitRunnable() {
                @Override
                public void run() {
                    deceased.spigot().respawn();
                    deceased.setGameMode(GameMode.ADVENTURE);
                    deceased.teleport(Config.getMainSpawnLocation());
                }
            }.runTaskLater(FlareonEvents.getPlugin(), 1L);
        }

        event.getDrops().add(SoulHeartCrystal.createSoulHeartCrystal(ItemUtils.legacyString(deceased.displayName())));

        AttributeInstance attr = deceased.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(20);
            deceased.setHealthScale(20);
        }

        event.deathMessage(Component.text("§k22 §c§lAUSLÖSCHUNG! §fEin Spieler ist gestorben §k22"));
        deceased.getWorld().strikeLightning(deceased.getLocation().add(0, 2.5, 0))
                .getPersistentDataContainer().set(namespacedKey, PersistentDataType.BYTE, (byte) 1);

        if (killer != null && !killer.equals(deceased)) {
            UUID uuid = killer.getUniqueId();
            pvpKillCounts.put(uuid, pvpKillCounts.getOrDefault(uuid, 0) + 1);
        }
    }

    public static Map<UUID, Integer> getPvpKillCounts() {
        return pvpKillCounts;
    }
}
