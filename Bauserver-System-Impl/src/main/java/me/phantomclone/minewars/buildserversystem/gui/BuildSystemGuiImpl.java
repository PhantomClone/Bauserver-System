package me.phantomclone.minewars.buildserversystem.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public record BuildSystemGuiImpl(JavaPlugin javaPlugin,
                                 ClickableItemStack fillClickableItemStack,
                                 ClickableItemStack myBuildWorldsClickableItemStack,
                                 ClickableItemStack allWorldsClickableItemStack,
                                 ClickableItemStack createNewWorldClickableItemStack) implements BuildSystemGui {

    private final static String WORLD_SKIN_DATA = "";
    private final static String PLUS_SKIN_DATA = "";

    public BuildSystemGuiImpl(JavaPlugin javaPlugin, GuiHandler guiHandler) {
        this(javaPlugin,
                new ClickableItemStack(new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE, Component.empty()).build(),
                        (player, clickType) -> {}),
                new ClickableItemStack(new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Meine Bauwelten")
                        .color(TextColor.color(11184810)))
                        .applyHeadTextures(javaPlugin, WORLD_SKIN_DATA).build(),
                        (player, clickType) -> guiHandler.allBuildWorldGui().openGuiForBuilder(player, aBoolean -> {})),
                new ClickableItemStack(new ItemStackBuilder(Material.OAK_SIGN, Component.text("Alle Bauwelten")
                        .color(TextColor.color(16755200))).build(),
                        (player, clickType) -> guiHandler.allBuildWorldGui().openGui(player, aBoolean -> {})),
                new ClickableItemStack(new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Neue welt erstellen")
                        .color(TextColor.color(5635925))).applyHeadTextures(javaPlugin, PLUS_SKIN_DATA).build(),
                        (player, clickType) -> guiHandler.createNewWorldGui().openGui(player))
                );
    }

    @Override
    public void openGui(Player player, boolean headBuilder) {
        new ClickableInventory(javaPlugin(), 3*9, Component.text("BuildSystem")).updateInventory()
                .setFillClickableItem(new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE, Component.empty()).build())
                .setClickableItem(11, myBuildWorldsClickableItemStack())
                .setClickableItem(13, headBuilder ? allWorldsClickableItemStack() : fillClickableItemStack())
                .setClickableItem(15, createNewWorldClickableItemStack()).applyUpdate().destroyOnClose(true)
                .registerListener().openInventory(player);
    }
}
