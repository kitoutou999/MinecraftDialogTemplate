# Minecraft Dialog Template

MinecraftUI is a Paper 1.21.8 plugin that explores Minecraft's native Dialog API. It adds a shop-style form to the pause menu and offers a resource pack that hides the dialog warning icon.

## Features

| Component | Behavior |
|---|---|
| Pause-menu dialog | Registers `minecraftui:shop` during bootstrap and references it from a bundled datapack |
| Form | Player-name text input, quantity slider from 1 to 64, express-delivery checkbox and four item categories |
| Confirmation | Sends the submitted values back to the player in chat; no purchase or inventory change is implemented |
| Presentation | Gradient title and a diamond-sword illustration in the dialog body |
| Resource pack | Three transparent warning-button textures, served over HTTP and offered on join |

## Getting started

Building requires JDK 21 and the included Gradle wrapper; running requires Paper 1.21.8 and a compatible Minecraft client. From the repository root:

```bash
./gradlew build
```

The build produces `build/libs/MinecraftUI-1.0-SNAPSHOT.jar`, including `resourcepack.zip`.

Copy the jar into the Paper server's `plugins/` directory and start the server. The plugin deploys its datapack into the first loaded world's `datapacks/minecraftui/` directory during enable; restart the server afterward so the datapack is loaded. Open the pause menu to access the registered dialog.

In the generated `config.yml`, set `resource-pack.host` to an address reachable by players and `resource-pack.port` to an available HTTP port, default `8123`. The host setting controls the advertised URL; the HTTP listener binds to all interfaces on that port. The pack is optional, is also offered to already connected players on enable, and uses a SHA-1 hash and derived UUID for cache invalidation.

## How it works

| File | Role |
|---|---|
| `MinecraftUIBootstrap.java` | Registers the dialog, inputs and confirmation callback through `RegistryEvents.DIALOG` |
| `MinecraftUI.java` | Deploys the datapack, starts the pack server and stops it on disable |
| `ResourcePackServer.java` | Serves the embedded zip and sends resource-pack requests |
| `src/main/resources/paper-plugin.yml` | Declares the Paper plugin and bootstrapper |
| `resourcepack/` | Transparent warning textures packaged by the Gradle build |

The original [README.txt](README.txt), in French, also records Dialog API experiments: runtime dialogs through `Dialog.create` and `player.showDialog`, dialog lists through `RegistrySet.valueSet(RegistryKey.DIALOG, collection)`, and `externalTitle` for pause-menu labels. These are development notes; the current source implements the static form.

## Limitations

The Dialog API is experimental and the interface text is in French. The original notes report a single custom pause-menu button, text-only action buttons, vertically arranged item illustrations at native size, and no live title or body animation. A datapack entry must match a registered dialog to avoid registry-loading errors. The warning icon is hidden only when the player accepts the optional pack.
