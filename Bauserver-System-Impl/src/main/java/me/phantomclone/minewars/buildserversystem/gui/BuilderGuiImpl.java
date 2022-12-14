package me.phantomclone.minewars.buildserversystem.gui;

import me.phantomclone.minewars.buildserversystem.skincache.SkinCache;
import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldData;
import me.phantomclone.minewars.buildserversystem.world.storage.BuilderStorage;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public record BuilderGuiImpl(JavaPlugin javaPlugin, BuilderStorage builderStorage, SkinCache skinCache, AtomicBoolean atomicBoolean)
        implements BuilderGui {

    public BuilderGuiImpl(JavaPlugin javaPlugin, BuilderStorage builderStorage, SkinCache skinCache) {
        this(javaPlugin, builderStorage, skinCache, new AtomicBoolean(false));
    }

    private static final String SCROLL_UP_SKIN_VALUE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWNkYjhmNDM2NTZjMDZjNGU4NjgzZTJlNjM0MWI0NDc5ZjE1N2Y0ODA4MmZlYTRhZmYwOWIzN2NhM2M2OTk1YiJ9fX0=";
    private static final String SCROLL_DOWN_SKIN_VALUE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjFlMWU3MzBjNzcyNzljOGUyZTE1ZDhiMjcxYTExN2U1ZTJjYTkzZDI1YzhiZTNhMDBjYzkyYTAwY2MwYmI4NSJ9fX0=";

    @Override
    public void openGui(Player player, BuildWorldData buildWorldData, List<UUID> builderList) {
        final List<Row> rowList = IntStream.range(0, (builderList.size() / 7) + (builderList.size() % 7 != 0 ? 0 : 1))
                .mapToObj(rowCounter ->
                        new Row(builderList.subList(rowCounter * 7, Math.min((rowCounter + 1) * 7, builderList.size())),
                                javaPlugin(), skinCache())).toList();
        ClickableInventory clickableInventory = new ClickableInventory(javaPlugin(), 2 + rowList.size(), Component.text("View Builders"));
        clickableInventory.destroyOnClose(true).registerListener();
        setRows(clickableInventory, 0, rowList)
                .setClickableItem(4, new ItemStackBuilder(Material.PLAYER_HEAD,
                        Component.text("Welten ersteller"))
                        .applyHeadTextures(javaPlugin(), skinCache().skinValueOfPlayerUuid(buildWorldData.worldCreatorUuid(), false))
                        .applyLore(Component.text(skinCache().playerNameOfPlayerUuid(buildWorldData.worldCreatorUuid(), false))).build(),
                        (player1, clickType) -> {}
                )
                .setClickableItem(6, generateAddBuilderClickableItemStack(buildWorldData.worldCreatorUuid(),
                        buildWorldData.worldUuid(), builderList))
                .setClickableItem(2, new ClickableItemStack(new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Remove Player"))
                        .build(),
                        (player1, clickType) -> clickableInventory.updateInventory().setItemStackEnchanted(2, !atomicBoolean().getAndSet(atomicBoolean().get()))))
                .applyUpdate().openInventory(player);

    }

    private ClickableItemStack generateAddBuilderClickableItemStack(UUID worldCreator, UUID buildWorldUuid, List<UUID> builderList) {
        return new ClickableItemStack(new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Add Builder")).build(),
                (player, clickType) -> {
            if (!player.getUniqueId().equals(worldCreator)) {
                player.sendMessage("Nur der Welten ersteller darf Builder hinzufügen!");
            } else {
                new AnvilGUI.Builder().title("Add Builder").text("PlayerName").onComplete((closedPlayer, playerName) -> {
                    final UUID uuid = skinCache().playerUuidOfPlayerName(playerName.replace(" ", ""), false);
                    if (uuid == null) {
                        closedPlayer.sendMessage("Diser Spieler gibt es nicht!");
                    } else if (builderList.contains(uuid)) {
                        closedPlayer.sendMessage("Diser Builder ist bereits in der BuildWelt!");
                    } else {
                        closedPlayer.sendMessage(Component.text("Builder ").append(Component.text(playerName))
                                .append(Component.text(" wird hinzugefügt...")));
                        builderStorage().insertBuilder(uuid, buildWorldUuid).whenComplete((aBoolean, throwable) ->
                                closedPlayer.sendMessage(aBoolean ? "Builder wurde hinzugefügt!" : "Builder konnte nicht hinzugefügt werden!"));
                        builderList.add(uuid);
                    }
                    return AnvilGUI.Response.close();
                }).plugin(javaPlugin()).open(player);
            }
        });
    }

    private ClickableInventory.ClickableItemStackBuilder setRows(ClickableInventory clickableInventory,
                                                                 int scroller, List<Row> rowList) {
        ClickableInventory.ClickableItemStackBuilder clickableItemStackBuilder = clickableInventory.updateInventory();
        final List<List<ItemStack>> lists = rowList.stream().skip(scroller).limit(5).map(Row::builderItemStackList)
                .toList();
        int i = 2;
        for (List<ItemStack> itemStackList : lists) {
            final int size = Math.min(itemStackList.size(), 9);
            for (int j = 0; j < size; j++) {
                clickableItemStackBuilder.setClickableItem(j + i*9, itemStackList.get(j), (player, clickType) -> {});
            }
            ++i;
        }
        if (scroller > 0) {
            clickableItemStackBuilder.setClickableItem(17,
                    new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Scroll Up"))
                    .applyHeadTextures(javaPlugin(), SCROLL_UP_SKIN_VALUE).build(),
                    (player, click) -> setRows(
                            clickableInventory, scroller - 1, rowList).applyUpdate()
            );
        }
        if (rowList.size() - scroller > 5) {
            clickableItemStackBuilder.setClickableItem(17,
                    new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Scroll Up"))
                            .applyHeadTextures(javaPlugin(), SCROLL_DOWN_SKIN_VALUE).build(),
                    (player, click) -> setRows(
                            clickableInventory, scroller + 1, rowList).applyUpdate()
            );
        }
        return clickableItemStackBuilder;
    }

    private record Row(List<ItemStack> builderItemStackList) {

        private Row(List<UUID> builderList, JavaPlugin javaPlugin, SkinCache skinCache) {
            this(builderList.stream()
                            .map(uuid -> new ItemStackBuilder(Material.PLAYER_HEAD,
                                    Component.text(skinCache.playerNameOfPlayerUuid(uuid, false)))
                                    .applyHeadTextures(javaPlugin, skinCache.skinValueOfPlayerUuid(uuid, false))
                                    .build()).toList()
                    );
        }

    }
}
