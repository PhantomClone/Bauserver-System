package me.phantomclone.minewars.buildserversystem.commands;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class RenameCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || !(sender instanceof Player player)
                || player.getInventory().getItemInMainHand().getType() == Material.AIR)
            return false;
        final ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        final ItemMeta itemMeta = itemInMainHand.getItemMeta();
        final String input = String.join(" ", args);
        final TextComponent component = LegacyComponentSerializer.legacyAmpersand().deserialize(input);
        itemMeta.displayName(component);
        itemInMainHand.setItemMeta(itemMeta);
        player.getInventory().setItemInMainHand(itemInMainHand);
        return true;
    }
}
