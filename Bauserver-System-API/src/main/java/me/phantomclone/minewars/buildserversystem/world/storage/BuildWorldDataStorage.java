package me.phantomclone.minewars.buildserversystem.world.storage;

import me.phantomclone.minewars.buildserversystem.world.BuildWorld;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BuildWorldDataStorage {

    CompletableFuture<Boolean> createTable();

    CompletableFuture<Optional<BuildWorldData>> loadBuildWorld(UUID buildWorldUuid);
    CompletableFuture<Boolean> insertBuildWorld(BuildWorld buildWorld);
    CompletableFuture<Boolean> deleteBuildWorld(UUID worldUuid);

    CompletableFuture<List<BuildWorldData>> buildWorldDataList();
    CompletableFuture<List<BuildWorldData>> buildWorldDataListOfBuilder(UUID builderUuid);

    CompletableFuture<List<BuildWorldData>> buildWorldDataListWithFilter(String shortGameType, UUID builderUuid, String worldName);

    CompletableFuture<Boolean> setEvaluate(UUID worldUuid, boolean evaluate);
    CompletableFuture<Boolean> setApproved(UUID worldUuid, UUID approvedHeadBuilder, long approvedTime);

}
