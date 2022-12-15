package me.phantomclone.minewars.buildserversystem.gametype;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public interface GameTyp {

    Component displayName();
    String shortName();
    int slot();
    Material material();

}
