package me.phantomclone.minewars.buildserversystem.world;

import de.chojo.sqlutil.conversion.UUIDConverter;
import me.phantomclone.minewars.buildserversystem.world.storage.*;
import me.phantomclone.minewars.buildserversystem.world.worldSetting.WorldType;
import org.apache.commons.io.FileUtils;
import org.bukkit.HeightMap;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public record BuildWorldHandlerImpl(JavaPlugin javaPlugin, List<BuildWorld> loadedBuildWorld,
                                    BuilderStorage builderStorage, BuildWorldDataStorage buildWorldDataStorage,
                                    BuildWorldStorage buildWorldStorage)
        implements BuildWorldHandler {

    public BuildWorldHandlerImpl(JavaPlugin javaPlugin, DataSource dataSource) {
        this(javaPlugin, dataSource, new ArrayList<>(), new BuilderStorageImpl(dataSource));
    }

    private BuildWorldHandlerImpl(JavaPlugin javaPlugin, DataSource dataSource, List<BuildWorld> loadedBuildWorld,
                                  BuilderStorage builderStorage) {
        this(javaPlugin, loadedBuildWorld, builderStorage,
                new BuildWorldDataStorageImpl(javaPlugin, dataSource, builderStorage),
                //new BuildWorldStorageImpl(javaPlugin, dataSource)
                new BuildWorldStorageHardDrive(javaPlugin)
        );
        try {
            buildWorldDataStorage.createTable().get();
            builderStorage.createTable().get();
            buildWorldStorage.createTable().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BuildWorldCreator buildWorldCreator() {
        return new BuildWorldCreatorImpl(this);
    }

    @Override
    public BuildWorld applyWorldCreator(BuildWorldCreator buildWorldCreator, UUID worldCreatorUuid) {
        final UUID worldUuid = UUID.randomUUID();
        final File folder = new File(javaPlugin().getServer().getWorldContainer(), worldUuid.toString());
        folder.mkdirs();
        final File uidFile = new File(folder, "uid.dat");
        try {
            if (!uidFile.createNewFile()) {
                throw new IOException("Can not create uidFile");
            }
            Files.write(uidFile.toPath(), UUIDConverter.convert(worldUuid));
        } catch (IOException e) {
            return null;
        }

        WorldCreator worldCreator = new WorldCreator(worldUuid.toString());
        worldCreator.environment(buildWorldCreator.getWorldType().worldEnvironment());
        worldCreator.type(buildWorldCreator.getWorldType().bukkitWorldType());
        if (buildWorldCreator.getWorldType() == WorldType.VOID)
            worldCreator.generator(new ChunkGenerator() {
                @Override
                public int getBaseHeight(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull HeightMap heightMap) {
                    return 256; //TODO TEST THIS... DOES IT MATTER?
                }
            });
        final World world = worldCreator.createWorld();
        if (world == null)
            throw new NullPointerException("World is null");
        buildWorldCreator.getWorldSettingSet().forEach(worldSetting -> worldSetting.applyWorldRule(world));

        final BuildWorld buildWorld = new BuildWorldDataImpl(
                world.getUID(),
                buildWorldCreator.getWorldName(),
                buildWorldCreator.getGameType(),
                worldCreatorUuid,
                System.currentTimeMillis()
        ).toBuildWorld(List.of(worldCreatorUuid), world);

        this.loadedBuildWorld.add(buildWorld);
        buildWorldDataStorage().insertBuildWorld(buildWorld)
                .whenComplete((aBoolean, throwable) ->
                        builderStorage().insertBuilder(worldCreatorUuid, buildWorld.buildWorldData().worldUuid()));
        return buildWorld;
    }

    @Override
    public CompletableFuture<BuildWorld> loadBuildWorld(BuildWorldData buildWorldData, boolean fromDatabase) {
        final Optional<BuildWorld> optionalBuildWorld = loadedBuildWorld().stream()
                .filter(buildWorld -> buildWorld.buildWorldData().worldUuid().equals(buildWorldData.worldUuid()))
                .findFirst();
        if (optionalBuildWorld.isPresent()) {
            return CompletableFuture.completedFuture(optionalBuildWorld.get());
        }


        if (buildWorldData.loadingCompletableFuture() != null)
            return buildWorldData.loadingCompletableFuture();
        final CompletableFuture<BuildWorld> completableFuture = new CompletableFuture<>();
        buildWorldData.setLoadingCompletableFuture(completableFuture);
        builderStorage().builderUuidOfWorld(buildWorldData.worldUuid())
                .whenComplete((uuids, throwable) -> handle(buildWorldData, fromDatabase, uuids, completableFuture));
        return completableFuture;
    }

    private void handle(BuildWorldData buildWorldData, boolean fromDatabase,
                                                 List<UUID> builders, CompletableFuture<BuildWorld> completableFuture) {
        if (!fromDatabase && new File(javaPlugin().getDataFolder(), buildWorldData.worldUuid().toString()).exists()) {
            javaPlugin().getServer().getScheduler().runTask(javaPlugin(), () -> {
                final BuildWorld buildWorld = buildWorldData.toBuildWorld(builders, new WorldCreator(buildWorldData.worldUuid().toString()).createWorld());
                this.loadedBuildWorld.add(buildWorld);
                completableFuture.complete(buildWorld);
            });
        } else {
            final File folder = new File(javaPlugin().getDataFolder(), buildWorldData.worldUuid().toString());
            if (folder.exists()) {
                try {
                    FileUtils.deleteDirectory(folder);
                } catch (IOException e) {
                    completableFuture.complete(null);
                    return;
                }
            }
            buildWorldStorage().loadBuildWorld(buildWorldData, builders).whenComplete((buildWorld, throwable) -> {
                this.loadedBuildWorld.add(buildWorld);
                completableFuture.complete(buildWorld);
            });
        }
    }

    @Override
    public boolean unloadWorld(BuildWorldData buildWorldData) {
        final World world = javaPlugin().getServer().getWorld(buildWorldData.worldUuid());
        if (world == null)
            return true;
        final boolean success = javaPlugin().getServer().unloadWorld(world, true);
        loadedBuildWorld().removeIf(buildWorld -> buildWorld.buildWorldData().worldUuid().equals(buildWorldData.worldUuid()));
        return success;
    }

    @Override
    public CompletableFuture<Boolean> saveBuildWorldInDB(BuildWorldData buildWorldData) {
        if (javaPlugin().getServer().getWorld(buildWorldData.worldUuid()) != null) {
            return CompletableFuture.completedFuture(false);
        }
        final File folder = new File(javaPlugin().getServer().getWorldContainer(), buildWorldData.worldUuid().toString());
        if (!folder.exists()) {
            return CompletableFuture.completedFuture(false);
        }
        return buildWorldStorage().saveBuildWorld(folder);
    }
}
