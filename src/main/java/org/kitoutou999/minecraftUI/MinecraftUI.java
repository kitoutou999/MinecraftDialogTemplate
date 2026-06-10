package org.kitoutou999.minecraftUI;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class MinecraftUI extends JavaPlugin {

    private static final String DATAPACK_NAME = "minecraftui";
    private static final String[] DATAPACK_FILES = {
        "datapack/pack.mcmeta",
        "datapack/data/minecraft/tags/dialog/pause_screen_additions.json"
    };

    private ResourcePackServer packServer;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        deployDatapack();
        startResourcePackServer();
    }

    @Override
    public void onDisable() {
        if (packServer != null) {
            packServer.stop();
            packServer = null;
        }
    }

    private void startResourcePackServer() {
        try (InputStream in = getResource("resourcepack.zip")) {
            if (in == null) {
                getLogger().warning("resourcepack.zip introuvable dans le jar.");
                return;
            }
            String host = getConfig().getString("resource-pack.host", "localhost");
            int port = getConfig().getInt("resource-pack.port", 8123);

            packServer = new ResourcePackServer(in.readAllBytes(), host, port);
            getServer().getPluginManager().registerEvents(packServer, this);
            // Couvre le hot reload : les joueurs déjà connectés n'émettent pas de PlayerJoinEvent
            getServer().getOnlinePlayers().forEach(packServer::send);

            getLogger().info("Resource pack servi sur http://" + host + ":" + port);
        } catch (Exception e) {
            getLogger().warning("Serveur de resource pack indisponible : " + e.getMessage());
        }
    }

    private void deployDatapack() {
        File worldFolder = getServer().getWorlds().get(0).getWorldFolder();
        File datapackFolder = new File(worldFolder, "datapacks/" + DATAPACK_NAME);

        deleteRecursively(datapackFolder);

        for (String resourcePath : DATAPACK_FILES) {
            String relative = resourcePath.substring("datapack/".length());
            File dest = new File(datapackFolder, relative);
            dest.getParentFile().mkdirs();
            try (InputStream in = getResource(resourcePath)) {
                if (in != null) Files.copy(in, dest.toPath());
            } catch (IOException e) {
                getLogger().warning("Impossible de copier " + resourcePath + " : " + e.getMessage());
            }
        }

        getLogger().info("Datapack deploye. Redemarrez le serveur pour appliquer les changements.");
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) deleteRecursively(child);
        }
        file.delete();
    }
}
