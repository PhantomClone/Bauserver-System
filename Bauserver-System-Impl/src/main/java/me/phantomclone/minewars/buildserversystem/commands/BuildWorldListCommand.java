package me.phantomclone.minewars.buildserversystem.commands;

import me.phantomclone.minewars.buildserversystem.BuildServerPlugin;
import me.phantomclone.minewars.buildserversystem.gui.ClickableInventory;
import me.phantomclone.minewars.buildserversystem.gui.ClickableItemStack;
import me.phantomclone.minewars.buildserversystem.gui.ItemStackBuilder;
import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

//TODO DELETE
public class BuildWorldListCommand implements CommandExecutor {
    private final static String MHF_ARROW_RIGHT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19";
    private final static String MHF_ARROW_LEFT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";

    private final BuildServerPlugin buildServerPlugin;

    public BuildWorldListCommand(BuildServerPlugin buildServerPlugin) {
        this.buildServerPlugin = buildServerPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length == 0) {
            this.buildServerPlugin.worldHandler().buildWorldDataStorage().buildWorldDataList()
                    .whenComplete((buildWorldData, throwable) -> openInventoryForPlayer(buildWorldData, player, 0));
        } else {
            final Player builderPlayer = this.buildServerPlugin.getServer().getPlayer(args[0]);
            if (builderPlayer != null)
                handle(player, builderPlayer.getUniqueId());
            else
                findPlayerUuid(args[0], uuid -> handle(player, uuid));
        }
        return true;
    }

    private void findPlayerUuid(String playerName, Consumer<UUID> callBack) {
        this.buildServerPlugin.getServer().getScheduler().runTaskAsynchronously(this.buildServerPlugin, () ->
            callBack.accept(this.buildServerPlugin.skinCache().playerUuidOfPlayerName(playerName, false))
        );

    }

    private void handle(Player player, UUID builderUuid) {
        this.buildServerPlugin.worldHandler().buildWorldDataStorage().buildWorldDataListOfBuilder(builderUuid)
                .whenComplete((buildWorldDataList, throwable) -> openInventoryForPlayer(buildWorldDataList, player, 0));
    }

    private void openInventoryForPlayer(List<BuildWorldData> buildWorldDataList, Player player, int page) {
        this.buildServerPlugin.getServer().getScheduler()
                .runTaskAsynchronously(this.buildServerPlugin,
                        () -> {
                            final AtomicInteger atomicInteger = new AtomicInteger(0);
                            final List<ClickableItemStack> itemStackList = buildWorldDataList.stream()
                                    .map(buildWorldData ->
                                            generateItemStack(buildWorldData, atomicInteger.getAndIncrement(),
                                                    buildWorldDataList)).toList();
                            this.buildServerPlugin.getServer().getScheduler().runTask(this.buildServerPlugin,
                                    () -> openInventory(player, itemStackList, page));
                        }
                );

    }

    private ClickableItemStack generateItemStack(BuildWorldData buildWorldData, int counter, List<BuildWorldData> buildWorldDataList) {
        final World world = this.buildServerPlugin.getServer().getWorld(buildWorldData.worldUuid());
        final TextComponent displayName = Component.text(buildWorldData.worldName()).color(TextColor.color(world == null ? 16733525 : 5635925));
        final ItemStackBuilder itemStackBuilder = new ItemStackBuilder(Material.PLAYER_HEAD, displayName);
        final String skinValueOfPlayerUuid = this.buildServerPlugin.skinCache().skinValueOfPlayerUuid(buildWorldData.worldCreatorUuid(), false);
        if (skinValueOfPlayerUuid != null)
            itemStackBuilder.applyHeadTextures(this.buildServerPlugin, skinValueOfPlayerUuid);
        return new ClickableItemStack(itemStackBuilder.build(), (player, clickType) ->
            new ClickableInventory(this.buildServerPlugin, 9, Component.text(buildWorldData.worldName())
                    .append(Component.text(" options")))
                    .destroyOnClose(true).registerListener().updateInventory()

                    .setClickableItem(0, new ItemStackBuilder(Material.BARRIER, Component.text("Zurück")
                            .color(TextColor.color(16733525))).build(),
                            (clickedPlayer, otherClickType) -> openInventoryForPlayer(buildWorldDataList, clickedPlayer, counter / 5*9))
                    .setClickableItem(2, new ItemStackBuilder(Material.ENDER_EYE, Component.text("Teleport"))
                            .build(), (clickedPlayer, otherClickType) -> teleportPlayerToWorld(buildWorldData.worldUuid(), player))
                    .setClickableItem(3, new ItemStackBuilder(Material.GREEN_DYE, Component.text("Load world")).build(),
                            (clickedPlayer, otherClickType) -> loadWorld(buildWorldData, clickedPlayer))
                    .setClickableItem(4, new ItemStackBuilder(Material.RED_DYE, Component.text("Unload world")).build(),
                            (clickedPlayer, otherClickType) -> unloadWorld(buildWorldData, clickedPlayer))
                    .setClickableItem(5, new ItemStackBuilder(Material.REDSTONE_LAMP, Component.text("Save world")).build(),
                            (clickedPlayer, otherClickType) -> saveWorld(buildWorldData, clickedPlayer))

                    .applyUpdate().openInventory(player)
        );
    }

    private void saveWorld(BuildWorldData buildWorldData, Player player) {
        player.sendMessage("Welt wird gespeichert...");
        this.buildServerPlugin.worldHandler().saveBuildWorldInDB(buildWorldData)
                .whenComplete((aBoolean, throwable) ->
                        player.sendMessage(aBoolean ? "Welt wurde gespeichert!" : "Welt konnte nicht gespeichert werden!"));

    }
    private void unloadWorld(BuildWorldData buildWorldData, Player player) {
        player.sendMessage("Welt wird entladen...");
        this.buildServerPlugin.worldHandler().unloadWorld(buildWorldData);
        player.sendMessage("Welt wurde entladen!");
    }

    private void loadWorld(BuildWorldData buildWorldData, Player player) {
        player.sendMessage("Welt wird geladen...");
        this.buildServerPlugin.worldHandler().loadBuildWorld(buildWorldData, false)
                .whenComplete((buildWorld, throwable) -> player.sendMessage("Welt wurde geladen!"));
    }

    private void teleportPlayerToWorld(UUID worldUuid, Player player) {
        final World world = this.buildServerPlugin.getServer().getWorld(worldUuid);
        if (world != null)
            player.teleport(world.getSpawnLocation());
        else
            player.sendMessage("Welt ist nicht geladen!");
    }

    private void openInventory(Player player, List<ClickableItemStack> itemStackList, int page) {
        final int layers = (int) (((double) ((itemStackList.size() - (page * 5 * 9)) / 9)) + 2);
        ClickableInventory.ClickableItemStackBuilder clickableItemStackBuilder = new ClickableInventory(this.buildServerPlugin,
                layers * 9, Component.text("Build Worlds")).updateInventory();
        if (page > 0) {
            clickableItemStackBuilder.setClickableItem(layers * 9 - 9,
                    new ItemStackBuilder(Material.PLAYER_HEAD,
                            Component.text("Zurück zu Page ").color(TextColor.color(16733525)).append(
                                    Component.text(page).color(TextColor.color(11184810)))
                    ).applyHeadTextures(this.buildServerPlugin, MHF_ARROW_LEFT).build(),
                    (clickedPlayer, clickType) -> openInventory(player, itemStackList, page - 1));
        }
        if (itemStackList.size() > 5 * 9 * (page + 1)) {
            clickableItemStackBuilder.setClickableItem(layers * 9 - 1,
                    new ItemStackBuilder(Material.PLAYER_HEAD,
                            Component.text("Zu nächsten Page ").color(TextColor.color(16733525)).append(
                                    Component.text(page + 1).color(TextColor.color(11184810)))
                    ).applyHeadTextures(this.buildServerPlugin, MHF_ARROW_RIGHT).build(),
                    (clickedPlayer, clickType) -> openInventory(player, itemStackList, page + 1));
        }
        final List<ClickableItemStack> itemStacks = itemStackList.stream().skip((long) page * 5 * 9).toList();
        for (int i = 0; i < itemStacks.size(); i++) {
            clickableItemStackBuilder.setClickableItem(i, itemStacks.get(i));
        }
        clickableItemStackBuilder.setClickableItem(layers * 9 - 5, new ItemStackBuilder(Material.BARRIER,
                        Component.text("Close").color(TextColor.color(16733525))).build(),
                (clickedPlayer, clickType) -> clickedPlayer.closeInventory());
        clickableItemStackBuilder.setFillClickableItem(
                new ItemStackBuilder(Material.GLASS_PANE, Component.empty()).build());
        clickableItemStackBuilder.applyUpdate().registerListener().destroyOnClose(true).openInventory(player);
    }

}
