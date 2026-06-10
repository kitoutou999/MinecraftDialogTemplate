package org.kitoutou999.minecraftUI;

import com.sun.net.httpserver.HttpServer;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Héberge le resource pack via un mini serveur HTTP embarqué
 * et l'envoie automatiquement aux joueurs qui se connectent.
 */
public final class ResourcePackServer implements Listener {

    private final HttpServer server;
    private final URI uri;
    private final String sha1Hex;
    private final UUID packId;

    ResourcePackServer(byte[] packBytes, String host, int port) throws Exception {
        byte[] sha1 = MessageDigest.getInstance("SHA-1").digest(packBytes);
        this.sha1Hex = HexFormat.of().formatHex(sha1);
        // UUID dérivé du hash : tout changement du pack force un re-téléchargement client
        this.packId = UUID.nameUUIDFromBytes(sha1);
        this.uri = URI.create("http://" + host + ":" + port + "/" + sha1Hex + ".zip");

        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/zip");
            exchange.sendResponseHeaders(200, packBytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(packBytes);
            }
        });
        this.server.start();
    }

    void stop() {
        server.stop(0);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        send(event.getPlayer());
    }

    void send(Player player) {
        player.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
            .packs(ResourcePackInfo.resourcePackInfo()
                .id(packId)
                .uri(uri)
                .hash(sha1Hex)
                .build())
            .required(false)
            .build());
    }
}
