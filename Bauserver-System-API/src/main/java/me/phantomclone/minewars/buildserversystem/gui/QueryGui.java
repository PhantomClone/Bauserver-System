package me.phantomclone.minewars.buildserversystem.gui;

import me.phantomclone.minewars.buildserversystem.gametype.GameTyp;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

public interface QueryGui {

    void open(Player player, GameTyp gameTyp, UUID builderUuid, String worldName, Consumer<Player> onClose);

}
