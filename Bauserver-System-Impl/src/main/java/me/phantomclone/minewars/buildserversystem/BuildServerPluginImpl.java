package me.phantomclone.minewars.buildserversystem;

import com.zaxxer.hikari.HikariDataSource;
import de.chojo.sqlutil.databases.SqlType;
import de.chojo.sqlutil.datasource.DataSourceCreator;
import me.phantomclone.minewars.buildserversystem.commands.BuildSystemCommand;
import me.phantomclone.minewars.buildserversystem.gametype.GameTypRegistry;
import me.phantomclone.minewars.buildserversystem.gametype.GameTypRegistryImpl;
import me.phantomclone.minewars.buildserversystem.gui.GuiHandler;
import me.phantomclone.minewars.buildserversystem.gui.GuiHandlerImpl;
import me.phantomclone.minewars.buildserversystem.skincache.SkinCache;
import me.phantomclone.minewars.buildserversystem.skincache.SkinCacheImpl;
import me.phantomclone.minewars.buildserversystem.world.BuildWorldHandler;
import me.phantomclone.minewars.buildserversystem.world.BuildWorldHandlerImpl;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class BuildServerPluginImpl extends BuildServerPlugin {

    private BuildWorldHandler buildWorldHandler;
    private GuiHandler guiHandler;
    private SkinCache skinCache;
    private GameTypRegistry gameTypRegistry;
    @Override
    public void onEnable() {
        if (!new File(getDataFolder(), "config.yml").exists())
            saveResource("config.yml", false);
        if (!new File(getDataFolder(), "Spielmodus.yml").exists())
            saveResource("Spielmodus.yml", false);

        try {
            this.gameTypRegistry = GameTypRegistryImpl.loadFromConfiguration(
                    YamlConfiguration.loadConfiguration(new File(getDataFolder(), "Spielmodus.yml"))
            );
        } catch (Exception exception) {
            exception.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        final HikariDataSource build = DataSourceCreator.create(SqlType.MARIADB)
                .configure(builder -> builder
                        .host(getConfig().getString("mariadb.storage.host"))
                        .database(getConfig().getString("mariadb.storage.database"))
                        .port(getConfig().getInt("mariadb.storage.port"))
                        .user(getConfig().getString("mariadb.storage.user"))
                        .password(getConfig().getString("mariadb.storage.password")))
                .create()
                .withMaximumPoolSize(20)
                .withMinimumIdle(2)
                .build();

        this.skinCache = new SkinCacheImpl(this);
        this.buildWorldHandler = new BuildWorldHandlerImpl(this, build);
        this.guiHandler = new GuiHandlerImpl(this, buildWorldHandler.builderStorage(), this.skinCache);
        final PluginCommand buildsystem = getCommand("buildsystem");
        if (buildsystem != null)
            buildsystem.setExecutor(new BuildSystemCommand(this.guiHandler));

    }

    @Override
    public BuildWorldHandler worldHandler() {
        return buildWorldHandler;
    }

    @Override
    public SkinCache skinCache() {
        return skinCache;
    }

    @Override
    public GuiHandler guiHandler() {
        return guiHandler;
    }

    @Override
    public GameTypRegistry gameTypRegistry() {
        return gameTypRegistry;
    }
}
