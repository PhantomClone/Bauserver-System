package me.phantomclone.minewars.buildserversystem.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ClickableInventory implements Listener {

    private final JavaPlugin javaPlugin;
    private boolean destroyOnClose;
    private final List<Player> playerOpenInventoryList;
    private final List<Player> preventPlayerClosingList;
    private BiConsumer<Player, ClickableInventory> onInventoryClose;

    private final Inventory inventory;
    private final Map<Integer, ClickableItemStack> integerClickableItemStackMap;
    private ClickableItemStack fillClickableItemStack;

    public ClickableInventory(JavaPlugin javaPlugin, int inventorySize, Component inventoryName) {
        this.javaPlugin = javaPlugin;
        this.playerOpenInventoryList = new ArrayList<>();
        this.preventPlayerClosingList = new ArrayList<>();
        this.integerClickableItemStackMap = new HashMap<>();
        this.inventory = javaPlugin.getServer().createInventory(null, inventorySize, inventoryName);
        this.fillClickableItemStack = new ClickableItemStack(new ItemStack(Material.AIR), (player, clickType) -> {});
        this.destroyOnClose = true;
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player && this.inventory.equals(event.getView().getTopInventory())) {
            this.playerOpenInventoryList.add(player);
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player && this.inventory.equals(event.getView().getTopInventory())) {
            this.playerOpenInventoryList.remove(player);
            if (this.onInventoryClose != null)
                this.onInventoryClose.accept(player, this);
            if (destroyOnClose())
                destroyListener();
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && playerHasOpenInventoryList().contains(player)) {
            event.setCancelled(true);
            if (event.getClickedInventory() == this.inventory && event.getCurrentItem() != null) {
                this.integerClickableItemStackMap.getOrDefault(event.getSlot(),
                        this.fillClickableItemStack).onClick().accept(player, event.getClick());
            }
        }
    }

    public ClickableInventory onInventoryClose(BiConsumer<Player, ClickableInventory> onClose) {
        this.onInventoryClose = onClose;
        return this;
    }

    public boolean destroyOnClose() {
        return destroyOnClose;
    }

    public ClickableInventory destroyOnClose(boolean destroyOnClose) {
        this.destroyOnClose = destroyOnClose;
        return this;
    }

    public boolean isPreventPlayerClosing(Player player) {
        return preventPlayerClosingList.contains(player);
    }

    public ClickableInventory preventPlayerClosing(Player player) {
        this.preventPlayerClosingList.removeIf(otherPlayer -> otherPlayer.equals(player));
        this.preventPlayerClosingList.add(player);
        return this;
    }

    public List<Player> playerHasOpenInventoryList() {
        return new ArrayList<>(this.playerOpenInventoryList);
    }

    public ClickableInventory registerListener() {
        this.javaPlugin.getServer().getPluginManager().registerEvents(this, this.javaPlugin);
        return this;
    }

    public ClickableInventory openInventory(Player player) {
        player.openInventory(this.inventory);
        return this;
    }

    public ClickableItemStackBuilder updateInventory() {
        return new ClickableItemStackBuilder(this, this.integerClickableItemStackMap);
    }

    private void destroyListener() {
        HandlerList.unregisterAll(this);
    }

    private ClickableInventory applyUpdate() {
        for (int i = 0; i < this.inventory.getSize(); i++) {
            final ItemStack itemStack = integerClickableItemStackMap.containsKey(i) ?
                    integerClickableItemStackMap.get(i).itemStack() : this.fillClickableItemStack.itemStack();
            this.inventory.setItem(i, itemStack);
        }
        playerHasOpenInventoryList().removeIf(player -> !player.isOnline());
        playerHasOpenInventoryList().forEach(Player::updateInventory);
        return this;
    }

    public record ClickableItemStackBuilder(ClickableInventory clickableInventory,
                                                 Map<Integer, ClickableItemStack> integerClickableItemStackMap) {

        public ClickableItemStackBuilder setClickableItem(int slot, Material material) {
            return setClickableItem(slot, material, (player, clickType) -> {});
        }

        public ClickableItemStackBuilder setClickableItem(int slot, Material material, BiConsumer<Player, ClickType> onClick) {
            return setClickableItem(slot, new ItemStack(material), onClick);
        }

        public ClickableItemStackBuilder setClickableItem(int slot, ItemStack itemStack, BiConsumer<Player, ClickType> onClick) {
            return setClickableItem(slot, new ClickableItemStack(itemStack, onClick));
        }

        public ClickableItemStackBuilder setItemStackEnchanted(int slot, boolean state) {
            ClickableItemStack clickableItemStack = integerClickableItemStackMap.get(slot);
            if (clickableItemStack != null && clickableItemStack.itemStack() != null
                    && clickableItemStack.itemStack().getType() != Material.AIR) {
                final ItemStack itemStack = clickableItemStack.itemStack();
                if (state) {
                    itemStack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.addEnchant(Enchantment.LUCK, 1, false);
                    itemStack.setItemMeta(itemMeta);
                } else if (itemStack.hasItemFlag(ItemFlag.HIDE_ENCHANTS)){
                    itemStack.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
                    itemStack.removeEnchantment(Enchantment.LUCK);
                }
            }
            return this;
        }

        public ClickableItemStackBuilder clearSetClickableItem() {
            integerClickableItemStackMap.clear();
            return this;
        }

        public ClickableItemStackBuilder setClickableItem(int slot, ClickableItemStack clickableItemStack) {
            integerClickableItemStackMap().put(slot, clickableItemStack);
            return this;
        }

        public ClickableItemStackBuilder setFillClickableItem(Material material) {
            return setFillClickableItem(new ItemStack(material));
        }

        public ClickableItemStackBuilder setFillClickableItem(ItemStack itemStack) {
            return setFillClickableItem(itemStack, (player, clickType) -> {});
        }

        public ClickableItemStackBuilder setFillClickableItem(Material material, BiConsumer<Player, ClickType> onClick) {
            return setFillClickableItem(new ItemStack(material), onClick);
        }

        public ClickableItemStackBuilder setFillClickableItem(ItemStack itemStack, BiConsumer<Player, ClickType> onClick) {
            return setFillClickableItem(new ClickableItemStack(itemStack, onClick));
        }

        public ClickableItemStackBuilder setFillClickableItem(ClickableItemStack fillClickableItemStack) {
            this.clickableInventory().fillClickableItemStack = fillClickableItemStack;
            return this;
        }

        public ClickableItemStackBuilder removeSetClickableItems() {
            integerClickableItemStackMap.clear();
            return this;
        }

        public ClickableInventory applyUpdate() {
            return clickableInventory().applyUpdate();
        }
    }
}
