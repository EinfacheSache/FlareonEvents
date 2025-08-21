package de.einfachesache.flareonevents.listener;

import de.einfachesache.api.AsyncExecutor;
import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.EventState;
import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.handler.GameHandler;
import de.einfachesache.flareonevents.item.ItemRecipe;
import de.einfachesache.flareonevents.item.misc.EventInfoBook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerJoinListener implements Listener {

    private static final UUID PACK_UUID = UUID.nameUUIDFromBytes("FLAREON_EVENTS_RESOURCE_PACK".getBytes(StandardCharsets.UTF_8 ));
    private static final String PACK_URL = "https://einfachesache.de/texturepack/Flareon-Events-V2.zip";
    private static final Component PACK_PROMPT =
            Component.text("Benötigtes Texturepack für ", NamedTextColor.GRAY)
                    .append(Component.text("Flareon Events", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .append(Component.text(" laden?", NamedTextColor.GRAY)));

    private final CompletableFuture<byte[]> packHash = new CompletableFuture<>();

    public PlayerJoinListener() {
        AsyncExecutor.getService().submit(() -> {
            try {
                packHash.complete(sha1(URI.create(PACK_URL).toURL()));
            } catch (Throwable t) {
                FlareonEvents.getLogManager().error("Pack-Hash fehlgeschlagen", t);
                packHash.completeExceptionally(t);
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        final boolean forced = !FlareonEvents.DEV_UUID.equals(player.getUniqueId()) && !player.isOp();

        packHash.handle((hash, ex) -> {
            player.getServer().getScheduler().runTask(FlareonEvents.getPlugin(), () -> {
                try {
                    if (ex == null && hash != null) {
                        player.setResourcePack(PACK_UUID, PACK_URL, hash, PACK_PROMPT, forced);
                    } else {
                        player.setResourcePack(PACK_UUID, PACK_URL, (byte[]) null, PACK_PROMPT, forced);
                    }
                } catch (Throwable t) {
                    FlareonEvents.getLogManager().error("setResourcePack fehlgeschlagen", t);
                }
            });
            return null;
        });

        event.joinMessage(player.displayName().append(Component.text(" ist dem server beigetreten", NamedTextColor.GRAY)));

        ItemRecipe.discoverRecipe(player);
        int stateId = Config.getEventState().getId();
        GameHandler.resetPlayer(player, stateId <= 2, stateId <= 1);

        if (!Config.isEventIsRunning()) {
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().setItem(8, EventInfoBook.createEventInfoBook());
            player.teleportAsync(Config.getEventState() == EventState.STARTING
                    ? GameHandler.getPlayerAssignedSpawn(player)
                    : Config.getMainSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    private static byte[] sha1(URL url) throws Exception {
        var conn = url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(15000);
        try (InputStream in = conn.getInputStream()) {
            var md = MessageDigest.getInstance("SHA-1");
            byte[] buf = new byte[16384];
            for (int r; (r = in.read(buf)) != -1; ) md.update(buf, 0, r);
            return md.digest();
        }
    }
}
