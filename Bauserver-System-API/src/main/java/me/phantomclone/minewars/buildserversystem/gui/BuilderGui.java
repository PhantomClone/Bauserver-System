package me.phantomclone.minewars.buildserversystem.gui;

import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldData;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface BuilderGui {

    void openGui(Player player, BuildWorldData buildWorldData, List<UUID> builders);

}
