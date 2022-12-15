package me.phantomclone.minewars.buildserversystem.gametype;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public record GameTypImpl(Component displayName, String shortName, int slot, Material material) implements GameTyp { }
