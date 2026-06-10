package org.kitoutou999.minecraftUI;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.DialogKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class MinecraftUIBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(
            RegistryEvents.DIALOG.compose().newHandler(event ->
                event.registry().register(
                    DialogKeys.create(Key.key("minecraftui:shop")),
                    builder -> builder
                        .base(DialogBase.builder(
                                MiniMessage.miniMessage().deserialize("<bold><gradient:#FF8C00:#FF2D55:#C800FF>MinecraftUI</gradient></bold>"))
                            .body(List.of(
                                DialogBody.item(new ItemStack(Material.DIAMOND_SWORD), null, true, true, 16, 16),
                                DialogBody.plainMessage(
                                    Component.text("Remplissez le formulaire puis confirmez.")
                                             .color(NamedTextColor.GRAY))
                            ))
                            .inputs(List.of(
                                DialogInput.text("pseudo",
                                        Component.text("Pseudo :").color(NamedTextColor.AQUA))
                                    .maxLength(16)
                                    .build(),
                                DialogInput.numberRange("quantite",
                                        Component.text("Quantite ").color(NamedTextColor.GREEN),
                                        1f, 64f)
                                    .initial(1f).step(1f)
                                    .build(),
                                DialogInput.bool("livraison",
                                        Component.text("Livraison express ?").color(NamedTextColor.LIGHT_PURPLE))
                                    .initial(false)
                                    .build(),
                                DialogInput.singleOption("categorie",
                                        Component.text("Categorie ").color(NamedTextColor.WHITE),
                                        List.of(
                                            SingleOptionDialogInput.OptionEntry.create("armes",      Component.text("Armes"),      true),
                                            SingleOptionDialogInput.OptionEntry.create("armures",    Component.text("Armures"),    false),
                                            SingleOptionDialogInput.OptionEntry.create("outils",     Component.text("Outils"),     false),
                                            SingleOptionDialogInput.OptionEntry.create("nourriture", Component.text("Nourriture"), false)
                                        ))
                                    .build()
                            ))
                            .canCloseWithEscape(true)
                            .build())
                        .type(DialogType.multiAction(List.of(
                            ActionButton.builder(Component.text("Confirmer").color(NamedTextColor.GREEN))
                                .action(DialogAction.customClick(
                                    (DialogActionCallback) (view, audience) -> {
                                        if (!(audience instanceof Player player)) return;

                                        String pseudo     = view.getText("pseudo");
                                        int quantite      = view.getFloat("quantite") != null ? view.getFloat("quantite").intValue() : 1;
                                        boolean livraison = Boolean.TRUE.equals(view.getBoolean("livraison"));
                                        String categorie  = view.getText("categorie");

                                        player.sendMessage(Component.text("=== Commande ===").color(NamedTextColor.GOLD));
                                        player.sendMessage(Component.text("Pseudo : "    + pseudo).color(NamedTextColor.AQUA));
                                        player.sendMessage(Component.text("Categorie : " + categorie).color(NamedTextColor.WHITE));
                                        player.sendMessage(Component.text("Quantite : "  + quantite).color(NamedTextColor.GREEN));
                                        player.sendMessage(Component.text("Express : "   + (livraison ? "Oui" : "Non")).color(NamedTextColor.LIGHT_PURPLE));
                                    },
                                    ClickCallback.Options.builder().build()
                                ))
                                .build()
                        )).build())
                )
            )
        );
    }
}
