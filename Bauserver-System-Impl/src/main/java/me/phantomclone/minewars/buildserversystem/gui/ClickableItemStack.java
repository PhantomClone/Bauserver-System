package me.phantomclone.minewars.buildserversystem.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

public class ClickableItemStack {

    private ItemStack itemStack;
    private BiConsumer<Player, ClickType> playerClickTypeBiConsumer;

    public ClickableItemStack(ItemStack itemStack, BiConsumer<Player, ClickType> playerClickTypeBiConsumer) {
        this.itemStack = itemStack;
        this.playerClickTypeBiConsumer = playerClickTypeBiConsumer;
    }

    public ItemStack itemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public BiConsumer<Player, ClickType> onClick() {
        return playerClickTypeBiConsumer;
    }

    public void setPlayerClickTypeBiConsumer(BiConsumer<Player, ClickType> playerClickTypeBiConsumer) {
        this.playerClickTypeBiConsumer = playerClickTypeBiConsumer;
    }
}
