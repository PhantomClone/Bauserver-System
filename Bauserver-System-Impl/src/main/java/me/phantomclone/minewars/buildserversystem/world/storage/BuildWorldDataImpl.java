package me.phantomclone.minewars.buildserversystem.world.storage;

import me.phantomclone.minewars.buildserversystem.world.BuildWorld;
import me.phantomclone.minewars.buildserversystem.world.BuildWorldImpl;
import org.bukkit.World;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record BuildWorldDataImpl(UUID worldUuid, String worldName, String gameType, UUID worldCreatorUuid, long created,
                                 Atomic<CompletableFuture<BuildWorld>> atomicCompletableFuture)
        implements BuildWorldData {

    public BuildWorldDataImpl(UUID worldUuid, String worldName, String gameType, UUID worldCreatorUuid, long created) {
        this(worldUuid, worldName, gameType, worldCreatorUuid, created, new Atomic<>());
    }

    @Override
    public BuildWorld toBuildWorld(List<UUID> builders, World world) {
        return new BuildWorldImpl(this, new HashSet<>(builders), world);
    }

    @Override
    public CompletableFuture<BuildWorld> loadingCompletableFuture() {
        return atomicCompletableFuture().t;
    }

    @Override
    public void setLoadingCompletableFuture(CompletableFuture<BuildWorld> loadingCompletableFuture) {
        atomicCompletableFuture().t = loadingCompletableFuture;
    }

    private static class Atomic<T> {
        private T t;
    }

}
