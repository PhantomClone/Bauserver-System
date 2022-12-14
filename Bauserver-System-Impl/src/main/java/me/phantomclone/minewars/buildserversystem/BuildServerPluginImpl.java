package me.phantomclone.minewars.buildserversystem;

import com.zaxxer.hikari.HikariDataSource;
import de.chojo.sqlutil.databases.SqlType;
import de.chojo.sqlutil.datasource.DataSourceCreator;
import me.phantomclone.minewars.buildserversystem.skincache.SkinCache;
import me.phantomclone.minewars.buildserversystem.skincache.SkinCacheImpl;
import me.phantomclone.minewars.buildserversystem.world.BuildWorldHandler;
import me.phantomclone.minewars.buildserversystem.world.BuildWorldHandlerImpl;

import java.io.File;

public class BuildServerPluginImpl extends BuildServerPlugin {

    private BuildWorldHandler buildWorldHandler;
    private SkinCache skinCache;
    @Override
    public void onEnable() {
        if (!new File(getDataFolder(), "config.yml").exists())
            saveResource("config.yml", false);
        if (!new File(getDataFolder(), "Spielmodus.yml").exists())
            saveResource("Spielmodus.yml", false);

        //TODO CHECK SpielModus.yml is correct

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

    }

    @Override
    public BuildWorldHandler worldHandler() {
        return buildWorldHandler;
    }

    @Override
    public SkinCache skinCache() {
        return skinCache;
    }
}
