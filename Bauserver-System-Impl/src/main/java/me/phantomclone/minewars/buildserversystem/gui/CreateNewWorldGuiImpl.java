package me.phantomclone.minewars.buildserversystem.gui;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.phantomclone.minewars.buildserversystem.BuildServerPlugin;
import me.phantomclone.minewars.buildserversystem.world.BuildWorldCreator;
import me.phantomclone.minewars.buildserversystem.world.worldSetting.WorldSetting;
import me.phantomclone.minewars.buildserversystem.world.worldSetting.WorldType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public record CreateNewWorldGuiImpl(BuildServerPlugin buildServerPlugin, Map<Player, AnvilInventory> playerAnvilInventoryMap) implements CreateNewWorldGui {

    private final static String MHF_ARROW_RIGHT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19";
    private final static String MHF_ARROW_LEFT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";
    private final static String GREEN_CHECKMARK = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDMxMmNhNDYzMmRlZjVmZmFmMmViMGQ5ZDdjYzdiNTVhNTBjNGUzOTIwZDkwMzcyYWFiMTQwNzgxZjVkZmJjNCJ9fX0=";

    public CreateNewWorldGuiImpl(BuildServerPlugin buildServerPlugin) {
        this(buildServerPlugin, new HashMap<>());
        setUpProtocolLib();
    }

    @Override
    public void openGui(Player player) {
        final BuildWorldCreator buildWorldCreator = buildServerPlugin.worldHandler().buildWorldCreator();
        openCreateWorld(buildWorldCreator, player);
    }

    private void openCreateWorld(BuildWorldCreator buildWorldCreator, Player player) {
        final ClickableInventory clickableInventory =
                new ClickableInventory(this.buildServerPlugin, 3*9,
                        Component.text("Neue Welt erstellen")
                                .color(TextColor.color(11184810))).destroyOnClose(true).registerListener();
        applyWorldType(buildWorldCreator.getWorldType(), clickableInventory.updateInventory().setFillClickableItem(
                new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE, Component.empty()).build()
        ).setClickableItem(9,
                new ItemStackBuilder(WorldType.FLAT.material(), Component.text("Flat")
                        .color(TextColor.color(5635925))).build(),
                (clickedPlayer, clickType) -> applyWorldType(WorldType.FLAT, clickableInventory.updateInventory(), buildWorldCreator)
        ).setClickableItem(11,
                new ItemStackBuilder(WorldType.STANDARD.material(), Component.text("Standard")
                        .color(TextColor.color(16755200))).build(),
                (clickedPlayer, clickType) -> applyWorldType(WorldType.STANDARD, clickableInventory.updateInventory(), buildWorldCreator)
        ).setClickableItem(13,
                new ItemStackBuilder(WorldType.VOID.material(), Component.text("Void")
                        .color(TextColor.color(11184810))).build(),
                (clickedPlayer, clickType) -> applyWorldType(WorldType.VOID, clickableInventory.updateInventory(), buildWorldCreator)
        ).setClickableItem(15,
                new ItemStackBuilder(WorldType.NETHER.material(), Component.text("Nether")
                        .color(TextColor.color(16733525))).build(),
                (clickedPlayer, clickType) -> applyWorldType(WorldType.NETHER, clickableInventory.updateInventory(), buildWorldCreator)
        ).setClickableItem(17,
                new ItemStackBuilder(WorldType.END.material(), Component.text("End")
                        .color(TextColor.color(43690))).build(),
                (clickedPlayer, clickType) -> applyWorldType(WorldType.END, clickableInventory.updateInventory(), buildWorldCreator)
        ).setClickableItem(26,
                new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Weiter")
                        .color(TextColor.color(16733525)))
                        .applyHeadTextures(this.buildServerPlugin, MHF_ARROW_RIGHT).build(),
                (clickedPlayer, clickType) -> {
                    if (buildWorldCreator.getWorldType() == null)
                        clickedPlayer.sendMessage(Component.text("Bitte wähle einen Welttype!")
                                .color(TextColor.color(16733525)));
                    else
                        firstNext(player, buildWorldCreator);
                }
        ).setClickableItem(18, new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Zurück")
                        .color(TextColor.color(16733525)))
                        .applyHeadTextures(this.buildServerPlugin, MHF_ARROW_LEFT).build(),
                        (clickedPlayer, clickType) -> buildServerPlugin.guiHandler()
                                .buildSystemGui().openGui(clickedPlayer, clickedPlayer.hasPermission("builder.headbuilder")))
                , buildWorldCreator).openInventory(player);
    }

    private ClickableInventory applyWorldType(WorldType worldType, ClickableInventory.ClickableItemStackBuilder clickableItemStackBuilder,
                                              BuildWorldCreator buildWorldCreator) {
        buildWorldCreator.applyWorldType(worldType);
        if (worldType == null) {
            return clickableItemStackBuilder.setItemStackEnchanted(9, false)
                    .setItemStackEnchanted(11, false).setItemStackEnchanted(13, false)
                    .setItemStackEnchanted(15, false).setItemStackEnchanted(17, false)
                    .applyUpdate();
        } else
            return switch (worldType) {
                case FLAT -> clickableItemStackBuilder.setItemStackEnchanted(9, true)
                        .setItemStackEnchanted(11, false).setItemStackEnchanted(13, false)
                        .setItemStackEnchanted(15, false).setItemStackEnchanted(17, false)
                        .applyUpdate();
                case STANDARD -> clickableItemStackBuilder.setItemStackEnchanted(9, false)
                        .setItemStackEnchanted(11, true).setItemStackEnchanted(13, false)
                        .setItemStackEnchanted(15, false).setItemStackEnchanted(17, false)
                        .applyUpdate();
                case VOID -> clickableItemStackBuilder.setItemStackEnchanted(9, false)
                        .setItemStackEnchanted(11, false).setItemStackEnchanted(13, true)
                        .setItemStackEnchanted(15, false).setItemStackEnchanted(17, false)
                        .applyUpdate();
                case NETHER -> clickableItemStackBuilder.setItemStackEnchanted(9, false)
                        .setItemStackEnchanted(11, false).setItemStackEnchanted(13, false)
                        .setItemStackEnchanted(15, true).setItemStackEnchanted(17, false)
                        .applyUpdate();
                case END -> clickableItemStackBuilder.setItemStackEnchanted(9, false)
                        .setItemStackEnchanted(11, false).setItemStackEnchanted(13, false)
                        .setItemStackEnchanted(15, false).setItemStackEnchanted(17, true)
                        .applyUpdate();
            };
    }

    private void firstNext(Player player, BuildWorldCreator buildWorldCreator) {
        final int inventorySize = buildServerPlugin().gameTypRegistry().inventorySize();
        final ClickableInventory clickableInventory = new ClickableInventory(this.buildServerPlugin, inventorySize,
                Component.text("Spielmodus wählen").color(TextColor.color(11184810)))
                .destroyOnClose(true).registerListener();
        final ClickableInventory.ClickableItemStackBuilder clickableItemStackBuilder = clickableInventory
                .updateInventory().setFillClickableItem(
                        new ItemStackBuilder(Material.GLASS_PANE, Component.empty()).build());

        buildServerPlugin().gameTypRegistry().gameTypeList().forEach(gameType -> {
            clickableItemStackBuilder.setClickableItem(gameType.slot(), new ItemStackBuilder(gameType.material(), gameType.displayName()).build(),
                    (otherPlayer, clickType) ->
                            applyGameType(gameType.shortName(), gameType.slot(), clickableInventory.updateInventory(), buildWorldCreator)
            );
            if (buildWorldCreator.getGameType() != null && buildWorldCreator.getGameType().equals(gameType.shortName())) {
                clickableItemStackBuilder.setItemStackEnchanted(gameType.slot(), true);
            }
        });
        clickableItemStackBuilder.setClickableItem(inventorySize - 1,
                        new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Weiter")
                                .color(TextColor.color(16733525)))
                                .applyHeadTextures(this.buildServerPlugin, MHF_ARROW_RIGHT).build(),
                        (clickedPlayer, clickType) -> {
                            if (buildWorldCreator.getGameType() == null)
                                clickedPlayer.sendMessage(Component.text("Bitte wähle einen Spielmodus aus!")
                                        .color(TextColor.color(16733525)));
                            else
                                secondNext(player, buildWorldCreator);
                        })
                .setClickableItem(inventorySize - 9,
                        new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Zurück")
                                .color(TextColor.color(16733525)))
                                .applyHeadTextures(this.buildServerPlugin, MHF_ARROW_LEFT).build(),
                        (clickedPlayer, clickType) -> openCreateWorld(buildWorldCreator, player)
                ).applyUpdate().openInventory(player);
    }
    private void applyGameType(String gameType, int slot,
                               ClickableInventory.ClickableItemStackBuilder clickableItemStackBuilder,
                               BuildWorldCreator buildWorldCreator) {
        buildWorldCreator.applyGameType(gameType);
        clickableItemStackBuilder.integerClickableItemStackMap().entrySet().stream()
                .filter(integerClickableItemStackEntry ->
                        integerClickableItemStackEntry.getValue().itemStack().hasItemFlag(ItemFlag.HIDE_ENCHANTS))
                .forEach(integerClickableItemStackEntry ->
                        clickableItemStackBuilder.setItemStackEnchanted(
                                integerClickableItemStackEntry.getKey(), false)
                );
        clickableItemStackBuilder.integerClickableItemStackMap().values().forEach(clickableItemStack -> {
            final ItemStack itemStack = clickableItemStack.itemStack();
            final ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null && itemMeta.hasEnchant(Enchantment.LUCK)) {
                itemMeta.removeEnchant(Enchantment.LUCK);
                itemMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
                itemStack.setItemMeta(itemMeta);
            }
        });
        clickableItemStackBuilder.setItemStackEnchanted(slot, true).applyUpdate();
        clickableItemStackBuilder.applyUpdate();
    }

    private void secondNext(Player player, BuildWorldCreator buildWorldCreator) {
        final ClickableInventory clickableInventory = new ClickableInventory(this.buildServerPlugin, 27,
                Component.text("Einstellung").color(TextColor.color(11184810)))
                .destroyOnClose(true).registerListener()
                .updateInventory().setFillClickableItem(new ItemStackBuilder(Material.GLASS_PANE, Component.empty()).build())
                .applyUpdate();
        changeSetting(null, clickableInventory, buildWorldCreator).openInventory(player);
    }

    private ClickableInventory changeSetting(WorldSetting worldSetting, ClickableInventory
            clickableInventory, BuildWorldCreator buildWorldCreator) {
        if (worldSetting != null) {
            if (!buildWorldCreator.getWorldSettingSet().removeIf(otherWorldSetting -> worldSetting == otherWorldSetting)) {
                buildWorldCreator.getWorldSettingSet().add(worldSetting);
            }
        }
        final Component timeComponent = buildWorldCreator.getWorldSettingSet().contains(WorldSetting.TIME_DOES_NOT_PASS) ?
                Component.text("Zeit vergeht").color(TextColor.color(5635925)) :
                Component.text("Zeit geht nicht").color(TextColor.color(16733525));
        final Component weatherComponent = buildWorldCreator.getWorldSettingSet().contains(WorldSetting.WEATHER_DOES_NOT_CHANGE) ?
                Component.text("Wetter ändert sich").color(TextColor.color(5635925)) :
                Component.text("Wetter ändert sich nicht").color(TextColor.color(16733525));
        final Component mobComponent = buildWorldCreator.getWorldSettingSet().contains(WorldSetting.MOBS_DOES_NOT_SPAWN) ?
                Component.text("Mobs spawnen").color(TextColor.color(5635925)) :
                Component.text("Mobs spawnen nicht").color(TextColor.color(16733525));
        final Component fireComponent = buildWorldCreator.getWorldSettingSet().contains(WorldSetting.FIRE_DOES_NOT_SPREAD) ?
                Component.text("Feuer greift über").color(TextColor.color(5635925)) :
                Component.text("Feuer greift nicht über").color(TextColor.color(16733525));
        final Component damageComponent = buildWorldCreator.getWorldSettingSet().contains(WorldSetting.PLAYERS_CAN_NOT_TAKE_DAMAGE) ?
                Component.text("Spieler können Schaden bekommen").color(TextColor.color(5635925)) :
                Component.text("Spieler können keinen Schaden bekommen").color(TextColor.color(16733525));

        ClickableInventory.ClickableItemStackBuilder clickableItemStackBuilder = clickableInventory.updateInventory()
                .setClickableItem(11,
                        new ItemStackBuilder(Material.CLOCK, timeComponent).build(), (otherPlayer, clickType) ->
                                changeSetting(WorldSetting.TIME_DOES_NOT_PASS, clickableInventory, buildWorldCreator)
                ).setClickableItem(12,
                        new ItemStackBuilder(Material.CORNFLOWER, weatherComponent).build(), (otherPlayer, clickType) ->
                                changeSetting(WorldSetting.WEATHER_DOES_NOT_CHANGE, clickableInventory, buildWorldCreator)
                ).setClickableItem(13,
                        new ItemStackBuilder(Material.CREEPER_SPAWN_EGG, mobComponent).build(), (otherPlayer, clickType) ->
                                changeSetting(WorldSetting.MOBS_DOES_NOT_SPAWN, clickableInventory, buildWorldCreator)
                ).setClickableItem(14,
                        new ItemStackBuilder(Material.FLINT_AND_STEEL, fireComponent).build(), (otherPlayer, clickType) ->
                                changeSetting(WorldSetting.FIRE_DOES_NOT_SPREAD, clickableInventory, buildWorldCreator)
                ).setClickableItem(15,
                        new ItemStackBuilder(Material.REDSTONE, damageComponent).build(), (otherPlayer, clickType) ->
                                changeSetting(WorldSetting.PLAYERS_CAN_NOT_TAKE_DAMAGE, clickableInventory, buildWorldCreator)
                ).setClickableItem(18,
                        new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Zurück")
                                .color(TextColor.color(16733525)))
                                .applyHeadTextures(this.buildServerPlugin, MHF_ARROW_LEFT).build(),
                        (clickedPlayer, clickType) -> firstNext(clickedPlayer, buildWorldCreator)
                ).setClickableItem(26,
                        new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("Beenden und der Welt einen Namen geben")
                                .color(TextColor.color(5635925)))
                                .applyHeadTextures(this.buildServerPlugin, GREEN_CHECKMARK).build(),
                        (clickedPlayer, clickType) -> thirdNext(clickedPlayer, buildWorldCreator));
        for (WorldSetting value : WorldSetting.values()) {
            clickableItemStackBuilder
                    .setItemStackEnchanted(value.slotPos(), buildWorldCreator.getWorldSettingSet().contains(value));
        }
        return clickableItemStackBuilder.applyUpdate();
    }

    private void thirdNext(Player player, BuildWorldCreator buildWorldCreator) {
        final ItemStack itemStackLeft = new ItemStack(buildWorldCreator.getWorldType().material());

        final AnvilGUI anvilGUI = new AnvilGUI.Builder()
                .plugin(this.buildServerPlugin)
                .itemLeft(itemStackLeft)
                .text(String.format("%s's Welt", player.getName()))
                .title("Bennene deine Welt")
                .onComplete((clickedPlayer, text) -> {
                    final String buildWorldName = text.replaceAll("[^a-zA-Z0-9]", "");
                    if (buildWorldName.length() == 0) {
                        reOpenAnvilUtil((AnvilInventory) player.getOpenInventory().getTopInventory(), player);
                        return AnvilGUI.Response.text("Der Name war zu kurz");
                    }
                    player.sendMessage(Component.text("Welt wird erstellt.").color(TextColor.color(5635925)));
                    this.buildServerPlugin.getServer().getScheduler().runTaskLater(this.buildServerPlugin, () ->
                            buildWorldCreator.createBuildWorld(buildWorldName, clickedPlayer.getUniqueId()), 5);
                    return AnvilGUI.Response.close();
                })
                .onClose(playerAnvilInventoryMap::remove)
                .open(player);

        reOpenAnvilUtil((AnvilInventory) anvilGUI.getInventory(), player);
    }

    private void reOpenAnvilUtil(AnvilInventory anvilInventory, Player player) {
        buildServerPlugin().getServer().getScheduler().runTaskLater(this.buildServerPlugin,
                () -> {
                    playerAnvilInventoryMap().put(player, anvilInventory);
                    anvilInventory.setResult(new ItemStackBuilder(Material.PLAYER_HEAD, Component.text("WorldName"))
                            .applyHeadTextures(this.buildServerPlugin, GREEN_CHECKMARK).build());
                    player.updateInventory();
                }, 5);
    }

    private void setUpProtocolLib() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(buildServerPlugin, PacketType.Play.Client.ITEM_NAME) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (playerAnvilInventoryMap().containsKey(event.getPlayer())) {
                    event.setCancelled(true);
                    final AnvilInventory anvilInventory = playerAnvilInventoryMap().get(event.getPlayer());
                    if (anvilInventory.getResult() != null) {
                        final ItemStack result = anvilInventory.getResult().clone();
                        final ItemMeta itemMeta = result.getItemMeta();
                        itemMeta.displayName(Component.text(event.getPacket().getStrings().getValues().get(0)
                                .replaceAll("[^a-zA-Z0-9]", "")));
                        result.setItemMeta(itemMeta);
                        anvilInventory.setResult(result);
                        event.getPlayer().updateInventory();
                    }
                }
            }
        });
    }
}
