package me.phantomclone.minewars.buildserversystem.world;

import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldData;
import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldDataStorage;
import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldStorage;
import me.phantomclone.minewars.buildserversystem.world.storage.BuilderStorage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BuildWorldHandler {

    BuildWorldCreator buildWorldCreator();

    BuildWorld applyWorldCreator(BuildWorldCreator buildWorldCreator,  UUID worldCreatorUuid);

    BuilderStorage builderStorage();
    BuildWorldDataStorage buildWorldDataStorage();
    BuildWorldStorage buildWorldStorage();

    CompletableFuture<BuildWorld> loadBuildWorld(BuildWorldData buildWorldData, boolean fromDatabase);

    boolean unloadWorld(BuildWorldData buildWorldData);

    CompletableFuture<Boolean> saveBuildWorldInDB(BuildWorldData buildWorldData);


}
