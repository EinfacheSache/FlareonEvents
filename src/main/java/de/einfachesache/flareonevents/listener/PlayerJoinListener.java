package de.einfachesache.flareonevents.listener;

import de.einfachesache.api.AsyncExecutor;
import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.event.EventState;
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

    private static final String PACK_URL_BASE = "https://einfachesache.de/texturepack/Flareon-Events-V3.zip";
    private static final UUID SESSION_UUID = UUID.randomUUID();
    private static final Component PACK_PROMPT =
            Component.text("Benötigtes Texturepack für ", NamedTextColor.GRAY)
                    .append(Component.text("Flareon Events", NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text(" laden?", NamedTextColor.GRAY));

    private final CompletableFuture<byte[]> packHash = new CompletableFuture<>();
    private volatile String sha1Hex = null;

    public PlayerJoinListener() {
        AsyncExecutor.getService().submit(() -> {
            try {
                byte[] hash = sha1(URI.create(PACK_URL_BASE).toURL());
                this.sha1Hex = toHex(hash);
                packHash.complete(hash);
                if (sha1Hex == null) {
                    FlareonEvents.getLogManager().warn("sha1Hex is null");
                }
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
                    String packUrl = (sha1Hex == null) ? PACK_URL_BASE : PACK_URL_BASE + "?v=" + sha1Hex;
                    UUID packUuid = (sha1Hex != null)
                            ? UUID.nameUUIDFromBytes((SESSION_UUID + sha1Hex).getBytes(StandardCharsets.UTF_8))
                            : UUID.nameUUIDFromBytes(SESSION_UUID.toString().getBytes(StandardCharsets.UTF_8));

                    if (ex == null && hash != null) {
                        player.setResourcePack(packUuid, packUrl, hash, PACK_PROMPT, forced);
                    } else {
                        player.setResourcePack(packUuid, packUrl, (byte[]) null, PACK_PROMPT, forced);
                    }

                } catch (Throwable t) {
                    FlareonEvents.getLogManager().error("setResourcePack fehlgeschlagen", t);
                }
            });
            return null;
        });

        event.joinMessage(player.displayName().append(Component.text(" ist dem Server beigetreten", NamedTextColor.GRAY)));

        ItemRecipe.discoverRecipe(player);
        int stateId = Config.getEventState().getId();
        GameHandler.resetPlayer(player, stateId <= 2, stateId <= 1);

        if (!Config.isEventIsRunning()) {
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().setItem(8, EventInfoBook.createEventInfoBook());
            player.teleportAsync(
                    Config.getEventState() == EventState.STARTING
                            ? GameHandler.getPlayerAssignedSpawn(player)
                            : Config.getMainSpawnLocation(),
                    PlayerTeleportEvent.TeleportCause.PLUGIN
            );
        }
    }

    private static byte[] sha1(URL url) throws Exception {
        var conn = url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("User-Agent", "FlareonEvents-ResourcePack-Loader");
        try (InputStream in = conn.getInputStream()) {
            var md = MessageDigest.getInstance("SHA-1");
            byte[] buf = new byte[16384];
            for (int r; (r = in.read(buf)) != -1; ) md.update(buf, 0, r);
            return md.digest();
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
