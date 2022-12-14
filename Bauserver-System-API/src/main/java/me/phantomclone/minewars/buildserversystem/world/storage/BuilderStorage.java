package me.phantomclone.minewars.buildserversystem.world.storage;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BuilderStorage {

    CompletableFuture<Boolean> createTable();

    CompletableFuture<Boolean> insertBuilder(UUID builderUuid, UUID worldUuid);
    CompletableFuture<Boolean> removeBuilder(UUID builderUuid, UUID worldUuid);
    CompletableFuture<List<UUID>> worldUuidOfBuilder(UUID builderUuid);
    CompletableFuture<List<UUID>> builderUuidOfWorld(UUID worldUuid);

}
