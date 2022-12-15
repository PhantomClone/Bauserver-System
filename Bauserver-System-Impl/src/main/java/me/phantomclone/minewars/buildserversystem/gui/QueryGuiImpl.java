package me.phantomclone.minewars.buildserversystem.gui;

import me.phantomclone.minewars.buildserversystem.BuildServerPlugin;
import me.phantomclone.minewars.buildserversystem.gametype.GameTyp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

public class QueryGuiImpl implements QueryGui {

    private final static String MHF_ARROW_LEFT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";
    private final static String GREEN_CHECKMARK = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDMxMmNhNDYzMmRlZjVmZmFmMmViMGQ5ZDdjYzdiNTVhNTBjNGUzOTIwZDkwMzcyYWFiMTQwNzgxZjVkZmJjNCJ9fX0=";
    private final static String WORLD_SKIN_DATA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODc5ZTU0Y2JlODc4NjdkMTRiMmZiZGYzZjE4NzA4OTQzNTIwNDhkZmVjZDk2Mjg0NmRlYTg5M2IyMTU0Yzg1In19fQ==";

    private final BuildServerPlugin buildServerPlugin;

    private GameTyp gameTyp;
    private UUID builderUuid;
    private String worldName;

    public QueryGuiImpl(BuildServerPlugin buildServerPlugin) {
        this.buildServerPlugin = buildServerPlugin;
    }

    @Override
    public void open(Player player, GameTyp gameTyp, UUID builderUuid, String worldName, Consumer<Player> onClose) {
        final ClickableInventory clickableInventory = new ClickableInventory(this.buildServerPlugin, 3 * 9,
                Component.text("Suche"));
        clickableInventory.destroyOnClose(true).registerListener().updateInventory().setFillClickableItem(
                new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE, Component.empty()).build()
        ).setClickableItem(9, new ClickableItemStack(
                new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Zurück")
                        .color(TextColor.color(16733525)))
                        .applyHeadTextures(this.buildServerPlugin, MHF_ARROW_LEFT).build(),
                (clickedPlayer, clickType) -> onClose.accept(clickedPlayer))
        ).setClickableItem(16,
                new ClickableItemStack(new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Filter anwenden"))
                        .applyHeadTextures(buildServerPlugin, GREEN_CHECKMARK).build(),
                        (clickedPlayer, clickType) ->
                                buildServerPlugin.worldHandler().buildWorldDataStorage()
                                        .buildWorldDataListWithFilter(this.gameTyp == null ? null : this.gameTyp.shortName(), this.builderUuid, this.worldName)
                                        .whenComplete((buildWorldDataList, throwable) ->
                                                        buildServerPlugin.guiHandler().allBuildWorldGui().openGui(clickedPlayer, aBoolean -> {}, buildWorldDataList, true)
                                        )
                )
        ).applyUpdate();
        setBuilderUuid(clickableInventory);
        setGameTyp(clickableInventory);
        setWorldName(clickableInventory);
        clickableInventory.openInventory(player);
    }

    public void setGameTyp(ClickableInventory clickableInventory) {
        final boolean hasGameType = this.gameTyp != null;
        clickableInventory.updateInventory().setClickableItem(12,
                new ClickableItemStack(new ItemStackBuilder(hasGameType ? gameTyp.material() : Material.BARRIER,
                       hasGameType ? this.gameTyp.displayName() : Component.text("Click um nach einen Spiel Modus zu Filtern")).build(),
                        (player, clickType) -> {
                    //TODO OPEN INVENTORY FOR GAMETYPE SELECTION
                        }))
                .applyUpdate();
    }

    public void setBuilderUuid(ClickableInventory clickableInventory) {
        final boolean hasBuilder = this.builderUuid != null;
        final ItemStack itemStack = hasBuilder ? new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Welten von ")
                .append(Component.text(buildServerPlugin.skinCache().playerNameOfPlayerUuid(builderUuid, false))))
                .applyHeadTextures(buildServerPlugin, buildServerPlugin.skinCache().skinValueOfPlayerUuid(builderUuid, false))
                .build() : new ItemStackBuilder(Material.BARRIER, Component.text("Click um nach einem Builder zu filtern")).build();
        clickableInventory.updateInventory().setClickableItem(13,
                new ClickableItemStack(itemStack,
                        (player, clickType) ->
                            new AnvilGUI.Builder().text("BuildName or Empty")
                                    .plugin(buildServerPlugin)
                                    .onComplete((closedPlayer, string) -> {
                                        if (string.isEmpty() || string.isBlank())
                                            builderUuid = null;
                                        else
                                            builderUuid = buildServerPlugin.skinCache().playerUuidOfPlayerName(string, false);
                                        return AnvilGUI.Response.close();
                                    })
                                    .onClose(closedPlayer -> {
                                        setBuilderUuid(clickableInventory);
                                        clickableInventory.registerListener().openInventory(player);
                                    })
                                    .open(player)
                        ))
                .applyUpdate();
    }

    public void setWorldName(ClickableInventory clickableInventory) {
        final boolean hasWorldName = this.worldName != null;

        final ItemStack itemStack = hasWorldName ? new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Weltenname: ")
                .append(Component.text(this.worldName)))
                .applyHeadTextures(buildServerPlugin, WORLD_SKIN_DATA)
                .build() : new ItemStackBuilder(Material.BARRIER, Component.text("Click um nach einem Weltennamen zu filtern")).build();

        clickableInventory.updateInventory().setClickableItem(14,
                new ClickableItemStack(itemStack,
                        (player, clickType) ->
                            new AnvilGUI.Builder().text("WorldName or Empty")
                                    .plugin(buildServerPlugin)
                                    .onComplete((closedPlayer, string) -> {
                                        if (string.isEmpty() || string.isBlank())
                                            this.worldName = null;
                                        else
                                            this.worldName = string;
                                        return AnvilGUI.Response.close();
                                    })
                                    .onClose(closedPlayer -> {
                                        setWorldName(clickableInventory);
                                        clickableInventory.registerListener().openInventory(player);
                                    })
                                    .open(player)
                        ))
                .applyUpdate();
    }
}
