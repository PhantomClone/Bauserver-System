package me.phantomclone.minewars.buildserversystem.world.storage;

import me.phantomclone.minewars.buildserversystem.gametype.GameTypRegistry;
import me.phantomclone.minewars.buildserversystem.skincache.SkinCache;
import me.phantomclone.minewars.buildserversystem.world.BuildWorld;
import net.kyori.adventure.text.Component;
import org.bukkit.World;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BuildWorldData {

    UUID worldUuid();
    String worldName();
    UUID worldCreatorUuid();
    String gameType();
    long created();
    boolean evaluate();
    UUID approvedHeadBuilder();
    long approvedTime();

    BuildWorld toBuildWorld(List<UUID> builders, World world);

    void setLoadingCompletableFuture(CompletableFuture<BuildWorld> loadingCompletableFuture);
    CompletableFuture<BuildWorld> loadingCompletableFuture();

    Component[] loreComponent(SkinCache skinCache, GameTypRegistry gameTypRegistry);

}
