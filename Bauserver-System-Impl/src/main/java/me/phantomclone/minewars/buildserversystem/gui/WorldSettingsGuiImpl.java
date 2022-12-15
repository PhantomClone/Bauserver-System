package me.phantomclone.minewars.buildserversystem.gui;

import me.phantomclone.minewars.buildserversystem.BuildServerPlugin;
import me.phantomclone.minewars.buildserversystem.gametype.GameTyp;
import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.DateFormat;
import java.util.Date;
import java.util.function.Consumer;

public record WorldSettingsGuiImpl(BuildServerPlugin buildServerPlugin) implements WorldSettingsGui {

    private final static String MHF_ARROW_LEFT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";
    private final static String LIGHT_UP_RIGHT_GREEN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjIxZmQ2ZWMxYmFlMGI1NGZkYTg1MjJlYWM1NDk5M2JhNDAwZTMxNDgwOGNlZjA4ODdkMjRiOTJjMTVjOTE5ZiJ9fX0=";
    private final static String LIGHT_UP_RIGHT_RED = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjAwZjU4ZjNmN2NlNzNkNGFhNmNjNjVmODU5YzM2Y2M3NTQzYTgyZTRmOWRhMDMxNzg0YmQ2MmM4ZDJiY2E2NyJ9fX0=";
    private final static String WORLD_SKIN_DATA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODc5ZTU0Y2JlODc4NjdkMTRiMmZiZGYzZjE4NzA4OTQzNTIwNDhkZmVjZDk2Mjg0NmRlYTg5M2IyMTU0Yzg1In19fQ==";

    @Override
    public void openGui(Player player, BuildWorldData buildWorldData, Consumer<Player> callOnBack) {
        final ClickableInventory clickableInventory = new ClickableInventory(buildServerPlugin(), 3 * 9,
                Component.text("World Settings"));
        clickableInventory.updateInventory()
                .setFillClickableItem(new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE, Component.empty()).build())
                .setClickableItem(18, new ClickableItemStack(
                        new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Zurück")
                                .color(TextColor.color(16733525)))
                                .applyHeadTextures(this.buildServerPlugin, MHF_ARROW_LEFT).build(),
                        (clickedPlayer, clickType) -> callOnBack.accept(clickedPlayer)))
                .setClickableItem(4, worldInfoClickableItemStack(buildWorldData))
                .setClickableItem(10, loadClickableItemStack(buildWorldData))
                .setClickableItem(12, unloadSaveClickableItemStack(buildWorldData))
                .setClickableItem(13, teleportClickableItemStack(buildWorldData))
                .setClickableItem(16, builderClickableItemStack(clickableInventory, buildWorldData))
                .setClickableItem(26, deleteClickableItemStack(buildWorldData))
                .applyUpdate().registerListener().openInventory(player);
    }

    private ClickableItemStack loadClickableItemStack(BuildWorldData buildWorldData) {
        return new ClickableItemStack(
                new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Lade Welt"))
                        .applyHeadTextures(buildServerPlugin(), LIGHT_UP_RIGHT_GREEN).build(),
                (player, clickType) -> buildServerPlugin().worldHandler().loadBuildWorld(buildWorldData, false)
                        .whenComplete((buildWorld, throwable) -> player.sendMessage("Welt wurde geladen!"))
        );
    }

    private ClickableItemStack unloadSaveClickableItemStack(BuildWorldData buildWorldData) {
        return new ClickableItemStack(
                new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Entlade und Speichern Welt"))
                        .applyHeadTextures(buildServerPlugin(), LIGHT_UP_RIGHT_RED).build(),
                (player, clickType) -> {
                    player.sendMessage("Welt wird entladen...");
                    this.buildServerPlugin.worldHandler().unloadWorld(buildWorldData);
                    player.sendMessage("Welt wurde entladen!");
                    player.sendMessage("Welt wird gespeichert...");
                    this.buildServerPlugin.worldHandler().saveBuildWorldInDB(buildWorldData)
                            .whenComplete((aBoolean, throwable) ->
                                    player.sendMessage(aBoolean ? "Welt wurde gespeichert!" : "Welt konnte nicht gespeichert werden!"));
                }
        );
    }

    private ClickableItemStack worldInfoClickableItemStack(BuildWorldData buildWorldData) {
        return new ClickableItemStack(
                new ItemStackBuilder(Material.PLAYER_HEAD, Component.text(buildWorldData.worldName()))
                        .applyLore(
                                Component.text("Welt Uuid: ").append(Component.text(buildWorldData.worldUuid().toString())),
                                Component.text("Game Modus: ").append(
                                        buildServerPlugin().gameTypRegistry().gameTypeList().stream()
                                                .filter(gameTyp -> gameTyp.shortName().equals(buildWorldData.gameType()))
                                                .map(GameTyp::displayName)
                                                .findFirst().orElse(Component.text("Not found!"))
                                ),
                                Component.text("Welt Ersteller: ").append(Component.text(buildServerPlugin().
                                        skinCache().playerNameOfPlayerUuid(buildWorldData.worldCreatorUuid(), false))),
                                Component.text("Erstellt am ").append(Component.text(DateFormat.getInstance()
                                        .format(new Date(buildWorldData.created()))))
                        )
                        .applyHeadTextures(buildServerPlugin(), WORLD_SKIN_DATA)
                        .build(),
                (player, clickType) -> {}
        );
    }
    private ClickableItemStack teleportClickableItemStack(BuildWorldData buildWorldData) {
        return new ClickableItemStack(
                new ItemStackBuilder(Material.ENDER_PEARL, Component.text("Teleport dich zu Welt")).build(),
                (player, clickType) -> buildServerPlugin().getServer().getWorlds().stream()
                        .filter(world -> world.getUID().equals(buildWorldData.worldUuid()))
                        .findAny().ifPresentOrElse(
                                world -> player.teleport(world.getSpawnLocation()),
                                () ->  player.sendMessage("Welt ist nicht geladen!")
                        )
        );
    }

    private ClickableItemStack builderClickableItemStack(ClickableInventory clickableInventory, BuildWorldData buildWorldData) {
        return new ClickableItemStack(
                new ItemStackBuilder(Material.WRITABLE_BOOK, Component.text("Builder übersicht")).build(),
                (player, clickType) ->
                    buildServerPlugin().worldHandler().builderStorage().builderUuidOfWorld(buildWorldData.worldUuid())
                            .whenComplete((uuids, throwable) -> {
                                if (uuids != null)
                                        buildServerPlugin().getServer().getScheduler().runTask(buildServerPlugin(), () ->
                                                buildServerPlugin().guiHandler().builderGui()
                                                        .openGui(player, buildWorldData, uuids,
                                                                onClose -> clickableInventory
                                                                        .registerListener().openInventory(onClose))
                                        );
                                    }
                            )
        );
    }

    private ClickableItemStack deleteClickableItemStack(BuildWorldData buildWorldData) {
        return new ClickableItemStack(
                new ItemStackBuilder(Material.BARRIER, Component.text("Lösche die Welt")).build(),
                (player, clickType) -> player.sendMessage("Not supported yet.")
        );
    }
}
