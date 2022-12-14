package me.phantomclone.minewars.buildserversystem.world;

import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldData;
import org.bukkit.World;

import java.util.Set;
import java.util.UUID;

public record BuildWorldImpl(BuildWorldData buildWorldData, Set<UUID> playerUuidSet, World world) implements BuildWorld {

}
