package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.FlareonEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class ResourcePackListener implements Listener {

    @EventHandler
    public void onPackStatus(PlayerResourcePackStatusEvent e) {
        switch (e.getStatus()) {

            case DOWNLOADED -> {}

            case ACCEPTED -> e.getPlayer().sendMessage(
                    FlareonEvents.PLUGIN_PREFIX.append(Component.text("Lade Resource Pack …", NamedTextColor.YELLOW))
            );

            case SUCCESSFULLY_LOADED -> e.getPlayer().sendMessage(
                    FlareonEvents.PLUGIN_PREFIX.append(Component.text("Resource Pack geladen. Viel Spaß!", NamedTextColor.GREEN))
            );

            case DECLINED -> e.getPlayer().sendMessage(
                    FlareonEvents.PLUGIN_PREFIX.append(Component.text("Resource Pack abgelehnt. Custom-Texturen sind deaktiviert.", NamedTextColor.RED))
            );

            case FAILED_DOWNLOAD -> e.getPlayer().sendMessage(
                    FlareonEvents.PLUGIN_PREFIX.append(Component.text("Download fehlgeschlagen. Bitte neu verbinden. Bleibt es bestehen, melde dich beim Team.", NamedTextColor.RED))
            );

            default -> e.getPlayer().sendMessage(
                    FlareonEvents.PLUGIN_PREFIX.append(Component.text("Status: " + e.getStatus().name(), NamedTextColor.GRAY))
            );
        }
    }
}
