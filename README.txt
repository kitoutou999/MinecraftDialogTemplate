MinecraftUI - Plugin Paper 1.21.8
=================================

Plugin Minecraft ciblant Paper 1.21.8, ecrit en Java 21.
Objectif : explorer et implementer les interfaces utilisateur natives de Minecraft (Dialog API).


ARCHITECTURE : COMMENT CA FONCTIONNE
======================================

1. Dialog API (menu pause -> shop)
-----------------------------------

- Bootstrapper (MinecraftUIBootstrap) : s'execute au demarrage du serveur, enregistre le
  dialog "minecraftui:shop" dans le registre Minecraft via RegistryEvents.DIALOG. Ce dialog
  contient les inputs (pseudo, quantite, livraison, categorie) et un bouton "Confirmer" avec
  un callback Java.

- Datapack : le fichier pause_screen_additions.json reference "minecraftui:shop". Minecraft
  ajoute alors un bouton directement dans le menu pause (touche Echap). Un seul clic ouvre
  le dialog.

- Callback : quand le joueur clique "Confirmer", le DialogActionCallback (lambda dans le
  bootstrapper) s'execute avec les valeurs saisies via DialogResponseView.


2. Resource pack (bouton warning invisible)
--------------------------------------------

Le bouton warning (triangle jaune) est impose cote client par Minecraft sur tous les dialogs
serveur. Pour le masquer :

- Le resource pack remplace les textures warning_button.png, warning_button_disabled.png et
  warning_button_highlighted.png par des images transparentes, dans :
  assets/minecraft/textures/gui/sprites/dialog/

- A chaque connexion d'un joueur (PlayerJoinEvent), le resource pack lui est envoye via
  un mini serveur HTTP embarque. Le client le met en cache et ne le retelecharge que si
  le pack change (detection par SHA-1).

- Le serveur HTTP ecoute sur le port 8123 par defaut (configurable).


DIALOG API - CE QUI EST POSSIBLE
==================================

Dialogs statiques (bootstrap)
-------------------------------
Enregistres via RegistryEvents.DIALOG dans le bootstrapper, disponibles des le demarrage.

- DialogBody.plainMessage(Component) : affiche du texte dans le body
- DialogBody.item(ItemStack, description, showDecorations, showTooltip, width, height) :
  affiche l'image d'un item dans le body (fonctionne en bootstrap)
- DialogInput : champs texte, slider numerique, case a cocher, liste deroulante
- DialogAction.customClick(DialogActionCallback, options) : callback Java execute au clic
- Titre avec degrade via MiniMessage :
  MiniMessage.miniMessage().deserialize("<gradient:#FF8C00:#FF2D55>Texte</gradient>")


Dialogs dynamiques (runtime)
------------------------------
Crees a la volee et envoyes au joueur pendant l'execution du serveur.

  Dialog.create(factory -> {
      var b = factory.empty();
      b.base(...).type(...);
  });
  player.showDialog(dialog);

- Memes capacites que les dialogs statiques
- DialogType.dialogList(RegistrySet<Dialog>, exitButton, columns, buttonWidth) :
  liste de sous-dialogs navigables en grille
- Pour un DialogListType dynamique, construire la RegistrySet avec :
  RegistrySet.valueSet(RegistryKey.DIALOG, listOfDialogs)
- player.closeDialog() : ferme le dialog courant et revient au precedent


Titre du dialog dans le menu pause
------------------------------------
Le champ externalTitle de DialogBase definit le texte affiche sur le bouton du menu pause.
Si null, utilise le champ title.


POINTS IMPORTANTS
==================

- Utilise paper-plugin.yml (pas plugin.yml) car le bootstrapper exige le systeme Paper plugin
- Toute l'API Dialog est @Experimental -> annoter les classes avec @SuppressWarnings("UnstableApiUsage")
- Si un dialog est reference dans le datapack (pause_screen_additions.json) mais n'est pas
  enregistre dans le bootstrapper, le serveur crashe au demarrage avec "Registry Loading"


LIMITES CONNUES
================

Menu pause (datapack)
----------------------
- pause_screen_additions : limite a 1 bouton custom. Minecraft deduplique les entrees
  identiques et n'affiche qu'un seul bouton meme avec plusieurs cles distinctes.


Images d'items (ItemDialogBody)
---------------------------------
- Les images s'affichent dans la zone body du dialog (decorative), pas sur les boutons
- Les boutons (ActionButton) n'acceptent que du texte (Component) comme label :
  impossible d'y mettre une image d'item nativement
- Le body rend les items en colonne verticale (un par ligne), pas en grille
- width et height definissent la taille du cadre conteneur, pas un redimensionnement
  de l'image : l'item reste a sa taille native Minecraft (16x16 pixels).
  Mettre width=16, height=16 pour un rendu correct sans espace vide autour.
- Pour simuler un item "cliquable" : utiliser DialogListType (bouton texte -> sous-dialog
  avec image) ou placer un bouton juste sous l'image dans le body


Animations
-----------
- Impossible nativement : le titre et le body sont des Component statiques envoyes une fois,
  sans mecanisme de mise a jour du dialog ouvert


registryValueSet()
-------------------
- Disponible uniquement dans le contexte bootstrap (DialogRegistryEntry.Builder)
- A l'execution, utiliser RegistrySet.valueSet(RegistryKey.DIALOG, collection) a la place
