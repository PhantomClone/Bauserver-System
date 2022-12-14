package me.phantomclone.minewars.buildserversystem.world.storage;

import me.phantomclone.minewars.buildserversystem.world.BuildWorld;
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

    BuildWorld toBuildWorld(List<UUID> builders, World world);

    void setLoadingCompletableFuture(CompletableFuture<BuildWorld> loadingCompletableFuture);
    CompletableFuture<BuildWorld> loadingCompletableFuture();

}
