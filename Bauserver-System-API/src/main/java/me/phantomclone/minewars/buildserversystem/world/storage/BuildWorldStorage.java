package me.phantomclone.minewars.buildserversystem.world.storage;

import me.phantomclone.minewars.buildserversystem.world.BuildWorld;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BuildWorldStorage {

    CompletableFuture<Boolean> createTable();

    CompletableFuture<Boolean> worldExists(UUID worldUuid);
    CompletableFuture<BuildWorld> loadBuildWorld(BuildWorldData buildWorldData, List<UUID> builderList);
    CompletableFuture<Boolean> saveBuildWorld(File worldFolder);

}
