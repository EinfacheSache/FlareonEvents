package de.einfachesache.flareonevents.handler;

import de.einfachesache.api.AsyncExecutor;
import de.einfachesache.flareonevents.FlareonEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TexturepackHandler {

    private static final String PACK_URL_BASE = "https://flareonevents.de/texturepack/Flareon-Events.zip";
    private static final UUID SESSION_UUID = UUID.randomUUID();
    private static final Component PACK_PROMPT =
            Component.text("Benötigtes Texturepack für ", NamedTextColor.GRAY)
                    .append(Component.text("Flareon Events", NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text(" laden?", NamedTextColor.GRAY));

    private static final CompletableFuture<byte[]> packHash = new CompletableFuture<>();
    private static volatile String sha1Hex = null;
    private static volatile UUID currentPackId = UUID.nameUUIDFromBytes(SESSION_UUID.toString().getBytes(StandardCharsets.UTF_8));

    public static void init() {
        computeHashAsync().whenComplete((hash, ex) -> {
            if (ex != null) return;
            updateComputedValues(hash);
        });
    }

    public static void setTexturepack(Player player) {
        final boolean forced = !FlareonEvents.DEV_UUID.equals(player.getUniqueId());
        packHash.handle((hash, ex) -> {
            player.getServer().getScheduler().runTask(FlareonEvents.getPlugin(), () -> {
                try {
                    String packUrl = buildUrl();
                    UUID packUuid = currentPackId;

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
    }

    public static void reloadAndPushAsync() {
        final UUID oldId = currentPackId;

        computeHashAsync().whenComplete((hash, ex) -> {
            if (ex != null) {
                FlareonEvents.getLogManager().error("Pack-Reload fehlgeschlagen, sende trotzdem Re-Apply ohne Hash", ex);
            } else {
                updateComputedValues(hash);
            }


            FlareonEvents.getPlugin().getServer().getScheduler().runTask(FlareonEvents.getPlugin(), () -> {
                String packUrl = buildUrl();
                UUID newId = currentPackId;
                byte[] hashBytes = (ex == null) ? hash : null;
                boolean forced;

                for (Player p : FlareonEvents.getPlugin().getServer().getOnlinePlayers()) {
                    try {
                        forced = !FlareonEvents.DEV_UUID.equals(p.getUniqueId());

                        try {
                            p.removeResourcePack(oldId);
                        } catch (Throwable ignored) {}

                        if (hashBytes != null) {
                            p.setResourcePack(newId, packUrl, hashBytes, PACK_PROMPT, forced);
                        } else {
                            p.setResourcePack(newId, packUrl, (byte[]) null, PACK_PROMPT, forced);
                        }

                    } catch (Throwable t) {
                        FlareonEvents.getLogManager().error("Re-Apply ResourcePack fehlgeschlagen für " + p.getName(), t);
                    }
                }
            });
        });
    }

    private static CompletableFuture<byte[]> computeHashAsync() {
        CompletableFuture<byte[]> f = new CompletableFuture<>();
        AsyncExecutor.getService().submit(() -> {
            try {
                byte[] hash = sha1(URI.create(PACK_URL_BASE).toURL());
                f.complete(hash);
            } catch (Throwable t) {
                f.completeExceptionally(t);
            }
        });
        return f;
    }

    private static void updateComputedValues(byte[] hash) {
        sha1Hex = toHex(hash);
        packHash.complete(hash);
        currentPackId = UUID.nameUUIDFromBytes((SESSION_UUID + sha1Hex).getBytes(StandardCharsets.UTF_8));
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

    private static String buildUrl() {
        return (sha1Hex == null) ? PACK_URL_BASE : PACK_URL_BASE + "?v=" + sha1Hex;
    }
}
