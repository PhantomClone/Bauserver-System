package me.phantomclone.minewars.buildserversystem.commands;

import me.phantomclone.minewars.buildserversystem.gui.GuiHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record BuildSystemCommand(GuiHandler guiHandler) implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player))
            return true;
        guiHandler().buildSystemGui().openGui(player, sender.hasPermission("builder.headbuilder"));
        return true;
    }

}
