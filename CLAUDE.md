# MinecraftUI - Plugin Paper 1.21.4

Plugin Minecraft en cours de développement ciblant **Paper 1.21.4**, écrit en Java.
L'objectif est d'explorer et implémenter des interfaces utilisateur custom intégrées à Minecraft (inventaires, menus, inputs).

## Stack technique

- **Serveur :** Paper 1.21.4
- **Language :** Java 21
- **Build :** Gradle (Kotlin DSL) avec le plugin `xyz.jpenilla.run-paper`
- **IDE :** IntelliJ IDEA avec le plugin Minecraft Dev

## Structure du projet

```
MinecraftUI/
├── src/main/java/org/kitoutou999/minecraftUI/
│   └── MinecraftUI.java          # Classe principale du plugin (onEnable / onDisable)
├── src/main/resources/
│   └── plugin.yml                # Métadonnées du plugin (nom, version, main class)
├── build.gradle.kts              # Config Gradle : dépendances, runServer, reloadPlugin
├── run/                          # Serveur Paper local (généré par runServer)
│   ├── plugins/                  # Dossier où le jar est copié pour le reload
│   └── server.properties         # RCON activé sur le port 25575 (password: devpassword)
└── gradle.properties
```

## Workflow de développement

Le setup est conçu pour itérer vite sans redémarrer le serveur.

**Lancer le serveur :**
Gradle > Run Configurations > **Run** (tâche `runServer`)
Démarre un Paper local sur `localhost:25565`.

**Recharger le plugin après une modif :**
Gradle > Tasks > minecraft > **reloadPlugin**
Cette tâche : compile le jar, le copie dans `run/plugins/`, envoie `/reload confirm` via RCON.
Aucun redémarrage du serveur nécessaire.

## Dépendances

- `io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT` (compileOnly)

## Points importants

- Le toolchain Java est fixé à 21 dans `build.gradle.kts`
- `plugin.yml` utilise `api-version: '1.21.4'` et `load: POSTWORLD`
- RCON est activé sur le serveur local uniquement (dev), ne pas exposer en production
