package me.phantomclone.minewars.buildserversystem.world.worldSetting;

import org.bukkit.Material;
import org.bukkit.World;

public enum WorldType {

    FLAT(Material.GRASS_BLOCK, World.Environment.NORMAL, org.bukkit.WorldType.FLAT),
    STANDARD(Material.STONE, World.Environment.NORMAL, org.bukkit.WorldType.NORMAL),
    VOID(Material.GLASS, World.Environment.NORMAL, org.bukkit.WorldType.NORMAL),
    NETHER(Material.NETHERRACK, World.Environment.NETHER, org.bukkit.WorldType.NORMAL),
    END(Material.END_STONE, World.Environment.THE_END, org.bukkit.WorldType.NORMAL);

    private final Material material;
    private final World.Environment worldEnvironment;
    private final org.bukkit.WorldType worldType;

    WorldType(Material material, World.Environment worldEnvironment, org.bukkit.WorldType worldType) {
        this.material = material;
        this.worldEnvironment = worldEnvironment;
        this.worldType = worldType;
    }

    public Material material() {
        return material;
    }

    public World.Environment worldEnvironment() {
        return this.worldEnvironment;
    }

    public org.bukkit.WorldType bukkitWorldType() {
        return worldType;
    }
}
