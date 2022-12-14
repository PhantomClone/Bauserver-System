package me.phantomclone.minewars.buildserversystem.gui;

import org.bukkit.entity.Player;

import java.util.function.Consumer;

public interface AllBuildWorldGui {

    void openGui(Player player, Consumer<Boolean> successConsumer);
    void openGuiForBuilder(Player player, Consumer<Boolean> successConsumer);

}
