package me.phantomclone.minewars.buildserversystem.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemStackBuilder {

    private final ItemStack itemStack;
    private ItemMeta itemMeta;

    public ItemStackBuilder(Material material, Component displayName) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
        this.itemMeta.displayName(displayName);
    }

    public ItemStackBuilder applyHeadTextures(JavaPlugin javaPlugin, ProfileProperty profileProperty) {
        SkullMeta skullMeta = (SkullMeta) itemMeta;
        PlayerProfile profile = javaPlugin.getServer().createProfile(UUID.randomUUID());
        profile.setProperty(profileProperty);
        skullMeta.setPlayerProfile(profile);
        this.itemStack.setItemMeta(skullMeta);
        this.itemMeta = skullMeta;
        return this;
    }

    public <T, Z> ItemStackBuilder applyNBTData(NamespacedKey key, PersistentDataType<T, Z> persistentDataType, Z value) {
        final PersistentDataContainer persistentDataContainer = this.itemMeta.getPersistentDataContainer();
        persistentDataContainer.set(key, persistentDataType, value);
        return this;
    }

    public ItemStackBuilder applyHeadTextures(JavaPlugin javaPlugin, String values) {
        SkullMeta skullMeta = (SkullMeta) itemMeta;
        PlayerProfile profile = javaPlugin.getServer().createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", values == null ? "" : values));
        skullMeta.setPlayerProfile(profile);
        this.itemStack.setItemMeta(skullMeta);
        this.itemMeta = skullMeta;
        return this;
    }

    public ItemStackBuilder applyLore(Component... loreComponent) {
        if (this.itemMeta == null)
            return this;
        final List<Component> lore = this.itemMeta.hasLore() ? this.itemMeta.lore() : new ArrayList<>();
        lore.addAll(List.of(loreComponent));
        this.itemMeta.lore(lore);
        return this;
    }

    public ItemStack build() {
        this.itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        this.itemStack.setItemMeta(itemMeta);
        return this.itemStack;
    }

}
