package me.phantomclone.minewars.buildserversystem;

import me.phantomclone.minewars.buildserversystem.gui.GuiHandler;
import me.phantomclone.minewars.buildserversystem.skincache.SkinCache;
import me.phantomclone.minewars.buildserversystem.world.BuildWorldHandler;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class BuildServerPlugin extends JavaPlugin {

    public abstract BuildWorldHandler worldHandler();
    public abstract SkinCache skinCache();
    public abstract GuiHandler guiHandler();

}
