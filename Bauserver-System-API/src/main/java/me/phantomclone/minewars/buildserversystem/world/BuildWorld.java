package me.phantomclone.minewars.buildserversystem.world;

import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldData;
import org.bukkit.World;

import java.util.Set;
import java.util.UUID;

public interface BuildWorld {

    BuildWorldData buildWorldData();
    Set<UUID> playerUuidSet();
    World world();

}
