package me.phantomclone.minewars.buildserversystem.gui;

import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldData;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public interface WorldSettingsGui {

    void openGui(Player player, BuildWorldData buildWorldData, Consumer<Player> callOnBack);

}
