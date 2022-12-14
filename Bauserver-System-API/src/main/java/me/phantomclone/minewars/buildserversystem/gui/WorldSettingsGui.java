package me.phantomclone.minewars.buildserversystem.gui;

import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldData;
import org.bukkit.entity.Player;

public interface WorldSettingsGui {

    void openGui(Player player, BuildWorldData buildWorldData);

}
