package me.phantomclone.minewars.buildserversystem.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GameModeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player))
            return true;
        final String gameMode = args.length == 0 ? label :
                ((label.equalsIgnoreCase("gm") || label.equalsIgnoreCase("gamemode"))
                        ? args[0] : "default");
        return switch (gameMode) {
            case "0" -> survival(player);
            case "1" -> creative(player);
            case "2" -> spectator(player);
            default -> false;
        };
    }

    private boolean survival(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.sendMessage(Component.text("Du bist nun im ").color(TextColor.color(11184810))
                .append(Component.text("SURVIVAL").color(TextColor.color(16755200))));
        return true;
    }

    private boolean creative(Player player) {
        player.setGameMode(GameMode.CREATIVE);
        player.sendMessage(Component.text("Du bist nun im ").color(TextColor.color(11184810))
                .append(Component.text("CREATIVE").color(TextColor.color(16755200))));
        return true;
    }

    private boolean spectator(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        player.sendMessage(Component.text("Du bist nun im ").color(TextColor.color(11184810))
                .append(Component.text("SPECTATOR").color(TextColor.color(16755200))));
        return true;
    }
}
