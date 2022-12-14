package me.phantomclone.minewars.buildserversystem.gui;

import me.phantomclone.minewars.buildserversystem.BuildServerPlugin;
import me.phantomclone.minewars.buildserversystem.skincache.SkinCache;
import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldData;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public record AllBuildWorldGuiImpl(BuildServerPlugin buildServerPlugin, SkinCache skinCache) implements AllBuildWorldGui {

    private static final String SCROLL_UP_SKIN_VALUE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWNkYjhmNDM2NTZjMDZjNGU4NjgzZTJlNjM0MWI0NDc5ZjE1N2Y0ODA4MmZlYTRhZmYwOWIzN2NhM2M2OTk1YiJ9fX0=";
    private static final String SCROLL_DOWN_SKIN_VALUE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjFlMWU3MzBjNzcyNzljOGUyZTE1ZDhiMjcxYTExN2U1ZTJjYTkzZDI1YzhiZTNhMDBjYzkyYTAwY2MwYmI4NSJ9fX0=";


    @Override
    public void openGui(Player player, Consumer<Boolean> successConsumer) {

    }

    @Override
    public void openGuiForBuilder(Player player, Consumer<Boolean> successConsumer) {

    }

    private ClickableInventory generateInventory(List<BuildWorldData> buildWorldDataList, boolean query) {
        final List<Row> rowList = IntStream.range(0, (buildWorldDataList.size() / 7) + (buildWorldDataList.size() % 7 != 0 ? 0 : 1))
                .mapToObj(rowCounter ->
                        new Row(buildWorldDataList.subList(rowCounter * 7, Math.min((rowCounter + 1) * 7, buildWorldDataList.size())),
                                buildServerPlugin(), skinCache())).toList();
        ClickableInventory clickableInventory = new ClickableInventory(buildServerPlugin(), rowList.size() + 2,
                Component.text("Bauwelten"));
        clickableInventory.destroyOnClose(true).registerListener();

        return setRows(clickableInventory, 0, rowList)

                .applyUpdate();
    }

    private ClickableInventory.ClickableItemStackBuilder setRows(ClickableInventory clickableInventory,
                                                                 int scroller, List<Row> rowList) {
        //TODO maybe -> interface + add in row clickconsumer...
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
                            .applyHeadTextures(buildServerPlugin(), SCROLL_UP_SKIN_VALUE).build(),
                    (player, click) -> setRows(
                            clickableInventory, scroller - 1, rowList).applyUpdate()
            );
        }
        if (rowList.size() - scroller > 5) {
            clickableItemStackBuilder.setClickableItem(17,
                    new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Scroll Up"))
                            .applyHeadTextures(buildServerPlugin(), SCROLL_DOWN_SKIN_VALUE).build(),
                    (player, click) -> setRows(
                            clickableInventory, scroller + 1, rowList).applyUpdate()
            );
        }
        return clickableItemStackBuilder;
    }

    private record Row(List<ItemStack> builderItemStackList) {

        private Row(List<BuildWorldData> builderList, JavaPlugin javaPlugin, SkinCache skinCache) {
            this(builderList.stream()
                    .map(buildWorldData -> new ItemStackBuilder(Material.PLAYER_HEAD,
                            Component.text(buildWorldData.worldName()))
                            .applyHeadTextures(javaPlugin, skinCache.skinValueOfPlayerUuid(buildWorldData.worldUuid(),
                                    false))
                            .build()).toList()
            );
        }

    }
}
