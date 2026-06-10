# MinecraftUI - Plugin Paper 1.21.8

Plugin Minecraft ciblant **Paper 1.21.8**, écrit en Java 21.
Objectif : explorer et implémenter les interfaces utilisateur natives de Minecraft (Dialog API).

## Stack technique

- **Serveur :** Paper 1.21.8
- **Language :** Java 21
- **Build :** Gradle (Kotlin DSL) avec le plugin `xyz.jpenilla.run-paper`
- **IDE :** IntelliJ IDEA avec le plugin Minecraft Dev

## Structure du projet

```
MinecraftUI/
├── src/main/java/org/kitoutou999/minecraftUI/
│   ├── MinecraftUI.java            # Classe principale : déploie le datapack + démarre le serveur HTTP du resource pack
│   ├── MinecraftUIBootstrap.java   # Bootstrapper Paper : enregistre le dialog "minecraftui:shop" dans le registre Minecraft
│   └── ResourcePackServer.java     # Serveur HTTP embarqué (JDK) : sert le resource pack et l'envoie aux joueurs à la connexion
├── src/main/resources/
│   ├── paper-plugin.yml            # Manifest du plugin (avec référence au bootstrapper)
│   ├── config.yml                  # Config : host/port du serveur HTTP du resource pack
│   ├── datapack/                   # Datapack bundlé dans le jar, déployé dans world/datapacks/ au démarrage
│   │   ├── pack.mcmeta
│   │   └── data/minecraft/tags/dialog/pause_screen_additions.json  # Ajoute minecraftui:shop au menu pause
│   └── (resourcepack.zip)          # Généré au build, embarqué dans le jar par la tâche buildResourcePack
├── resourcepack/                   # Sources du resource pack
│   ├── pack.mcmeta
│   └── assets/minecraft/textures/gui/sprites/dialog/
│       ├── warning_button.png              # Transparents : masquent le bouton ⚠ dans les dialogs
│       ├── warning_button_disabled.png
│       └── warning_button_highlighted.png
├── build.gradle.kts
└── run/                            # Serveur Paper local (généré par runServer)
    └── server.properties           # RCON activé sur port 25575 (password: devpassword)
```

## Architecture : comment ça fonctionne

### Dialog API (menu pause -> shop)

1. **Bootstrapper** (`MinecraftUIBootstrap`) : s'exécute au démarrage du serveur, enregistre le dialog `minecraftui:shop` dans le registre Minecraft via `RegistryEvents.DIALOG`. Ce dialog contient les inputs (pseudo, quantité, livraison, catégorie) et un bouton "Confirmer" avec un callback Java.

2. **Datapack** : le fichier `pause_screen_additions.json` référence `minecraftui:shop`. Minecraft ajoute alors un bouton "MinecraftUI" directement dans le menu pause (touche Echap). Un seul clic ouvre le dialog.

3. **Callback** : quand le joueur clique "Confirmer", le `DialogActionCallback` (lambda dans le bootstrapper) s'exécute avec les valeurs saisies via `DialogResponseView`.

### Resource pack (bouton ⚠ invisible)

Le bouton warning est imposé côté client par Minecraft sur tous les dialogs serveur. Pour le masquer :

1. **Build** : la tâche Gradle `buildResourcePack` zippe `resourcepack/` en `build/resourcepack/MinecraftUI-ResourcePack.zip`. La tâche `jar` embarque ce zip sous le nom `resourcepack.zip`.

2. **Runtime** : `ResourcePackServer` lit `resourcepack.zip` depuis le jar, calcule son SHA-1, et lance un mini serveur HTTP sur `localhost:8123` (configurable dans `config.yml`). À chaque `PlayerJoinEvent`, le pack est proposé au joueur. Le client le met en cache (UUID dérivé du hash) et ne le retélécharge que si le pack change.

## Workflow de développement

**Lancer le serveur :**
Gradle > Tasks > run > **runServer**
Démarre Paper 1.21.8 sur `localhost:25565`.

**Recharger le plugin après une modif :**
Gradle > Tasks > minecraft > **reloadPlugin**
Compile, copie le jar dans `run/plugins/`, envoie `/reload confirm` via RCON.

**Attention :** les modifications du bootstrapper (dialog) et du datapack nécessitent un **redémarrage complet** du serveur (pas seulement un reload).

## Dépendances

- `io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT` (compileOnly)

## Points importants

- Utilise `paper-plugin.yml` (pas `plugin.yml`) car le bootstrapper exige le système Paper plugin
- Toute l'API Dialog est `@Experimental` -> annoter les classes concernées avec `@SuppressWarnings("UnstableApiUsage")`
- Le serveur HTTP du resource pack écoute sur le port `8123` par défaut : s'assurer qu'il n'est pas bloqué par un firewall
- RCON activé sur le serveur local uniquement (dev), ne pas exposer en production
- Si un dialog est référencé dans le datapack (`pause_screen_additions.json`) mais n'est pas enregistré dans le bootstrapper, le serveur crashe au démarrage avec `Registry Loading`
- Le jar dans `run/plugins/` et le jar du build (`build/libs/`) ne doivent pas coexister : supprimer `run/plugins/MinecraftUI.jar` si `runServer` charge le jar depuis `build/libs/` directement

## Dialog API - Ce qui est possible

### Dialogs statiques (bootstrap)
Enregistrés via `RegistryEvents.DIALOG` dans le bootstrapper, disponibles dès le démarrage.
- `DialogBody.plainMessage(Component)` : texte
- `DialogBody.item(ItemStack, description, showDecorations, showTooltip, width, height)` : image d'item (**fonctionne en bootstrap**)
- `DialogInput` : champs texte, slider numérique, case à cocher, liste déroulante
- `DialogAction.customClick(DialogActionCallback, options)` : callback Java au clic
- Titre avec dégradé via MiniMessage : `MiniMessage.miniMessage().deserialize("<gradient:#FF8C00:#FF2D55>Texte</gradient>")`

### Dialogs dynamiques (runtime)
Créés à la volée avec `Dialog.create(factory -> { var b = factory.empty(); b.base(...).type(...); })` et envoyés avec `player.showDialog(dialog)`.
- Mêmes capacités que les dialogs statiques
- `DialogType.dialogList(RegistrySet<Dialog>, exitButton, columns, buttonWidth)` : liste de sous-dialogs navigables
- Pour un `DialogListType` dynamique, construire la `RegistrySet` avec `RegistrySet.valueSet(RegistryKey.DIALOG, listOfDialogs)` — **ne pas utiliser** `builder.registryValueSet()` qui est réservé au bootstrap
- `player.closeDialog()` : ferme le dialog courant et revient au précédent

### Titre du dialog dans le menu pause
Le champ `externalTitle` de `DialogBase` définit le texte affiché sur le bouton du menu pause (si null, utilise `title`).

## Limites connues

### Menu pause (datapack)
- `pause_screen_additions` : **limité à 1 bouton custom** — Minecraft déduplique les entrées identiques et n'affiche qu'un seul bouton même avec plusieurs clés distinctes

### Images d'items (`ItemDialogBody`)
- Les images s'affichent dans la **zone body** du dialog (décorative), pas sur les boutons
- Les boutons (`ActionButton`) n'acceptent que du texte (`Component`) comme label — impossible d'y mettre une image d'item nativement
- Le body rend les items en **colonne verticale** (un par ligne), pas en grille
- `width` et `height` définissent la taille du cadre conteneur, pas un redimensionnement de l'image — l'item reste à sa taille native Minecraft (16x16 pixels) ; mettre `width=16, height=16` pour un rendu correct sans espace vide
- Pour simuler un item "cliquable" : utiliser `DialogListType` (bouton texte -> sous-dialog avec image) ou placer un bouton juste sous l'image dans le body

### Animations
- Impossible nativement : le titre et le body sont des `Component` statiques envoyés une fois, sans mécanisme de mise à jour du dialog ouvert

### `registryValueSet()`
- Disponible uniquement dans le contexte bootstrap (`DialogRegistryEntry.Builder`)
- À l'exécution, utiliser `RegistrySet.valueSet(RegistryKey.DIALOG, collection)` à la place
