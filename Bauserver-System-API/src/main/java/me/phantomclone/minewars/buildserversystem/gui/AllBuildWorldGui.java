package me.phantomclone.minewars.buildserversystem.gui;

import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldData;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

public interface AllBuildWorldGui {

    void openGui(Player player, Consumer<Boolean> successConsumer);

    void openGui(Player player, Consumer<Boolean> successConsumer, List<BuildWorldData> buildWorldData, boolean query);
    void openGuiForBuilder(Player player, Consumer<Boolean> successConsumer);

}
