package me.phantomclone.minewars.buildserversystem.world.storage;

import de.chojo.sqlutil.conversion.UUIDConverter;
import de.chojo.sqlutil.wrapper.QueryBuilder;
import de.chojo.sqlutil.wrapper.stage.StatementStage;
import me.phantomclone.minewars.buildserversystem.world.BuildWorld;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record BuildWorldDataStorageImpl(JavaPlugin javaPlugin, BuilderStorage builderStorage,
                                    StatementStage<?> createTableStatementStage,
                                    StatementStage<BuildWorldData> loadBuildWorldStatementStage,
                                    StatementStage<?> insertBuildWorldStatementStage,
                                    StatementStage<?> deleteBuildWorldStatementStage,
                                    StatementStage<BuildWorldData> buildWorldDataListStatementStage,
                                    StatementStage<BuildWorldData> buildWorldDataListOfBuilderStatementStage)
        implements BuildWorldDataStorage {

    public BuildWorldDataStorageImpl(JavaPlugin javaPlugin, DataSource dataSource, BuilderStorage builderStorage) {
        this(javaPlugin, builderStorage,
                QueryBuilder.builder(dataSource).defaultConfig()
                        .query("CREATE TABLE IF NOT EXISTS BuildWorld(worldUuid BINARY(16) NOT NULL, worldName VARCHAR(255) NOT NULL, " +
                                "gameType VARCHAR(10) NOT NULL, worldCreatorUuid BINARY(16) NOT NULL, " +
                                "created TIMESTAMP NOT NULL, PRIMARY KEY (worldUuid))"),
                QueryBuilder.builder(dataSource, BuildWorldData.class).defaultConfig()
                        .query("SELECT worldName, gameType, worldCreatorUuid, created FROM BuildWorld WHERE worldUuid=? LIMIT=1"),
                QueryBuilder.builder(dataSource).defaultConfig()
                        .query("INSERT INTO BuildWorld(worldUuid, worldName, gameType, worldCreatorUuid, created) VALUES(?, ?, ?, ?, ?)"),
                QueryBuilder.builder(dataSource).defaultConfig()
                        .query("DELETE FROM BuildWorld WHERE worldUuid=?"),
                QueryBuilder.builder(dataSource, BuildWorldData.class).defaultConfig()
                        .query("SELECT worldUuid, worldName, gameType, worldCreatorUuid, created FROM BuildWorld"),
                QueryBuilder.builder(dataSource, BuildWorldData.class).defaultConfig()
                        .query("SELECT bw.worldUuid, bw.worldName, bw.gameType, bw.worldCreatorUuid, bw.created FROM BuildWorld bw, Builder b " +
                                "WHERE bw.worldUuid = b.worldUuid AND b.builderUuid = ?")
        );
    }

    @Override
    public CompletableFuture<Boolean> createTable() {
        return createTableStatementStage().emptyParams().update().execute().thenApply(integer -> integer != 0);
    }

    @Override
    public CompletableFuture<Optional<BuildWorldData>> loadBuildWorld(UUID worldUuid) {
         return loadBuildWorldStatementStage()
                .paramsBuilder(paramBuilder -> paramBuilder.setBytes(UUIDConverter.convert(worldUuid)))
                .readRow(resultSet -> new BuildWorldDataImpl(
                        worldUuid,
                        resultSet.getString("worldName"),
                        resultSet.getString("gameType"),
                        UUIDConverter.convert(resultSet.getBytes("worldCreatorUuid")),
                        resultSet.getTimestamp("created").getTime()
                        )).first();
    }

    @Override
    public CompletableFuture<Boolean> insertBuildWorld(BuildWorld buildWorld) {
        return insertBuildWorldStatementStage().paramsBuilder(paramBuilder -> paramBuilder
                .setBytes(UUIDConverter.convert(buildWorld.buildWorldData().worldUuid()))
                .setString(buildWorld.buildWorldData().worldName())
                .setString(buildWorld.buildWorldData().gameType())
                .setBytes(UUIDConverter.convert(buildWorld.buildWorldData().worldCreatorUuid()))
                .setTimestamp(new Timestamp(buildWorld.buildWorldData().created()))
        ).insert().execute().thenApply(integer -> integer != 0);
    }

    @Override
    public CompletableFuture<Boolean> deleteBuildWorld(UUID worldUuid) {
        return deleteBuildWorldStatementStage()
                .paramsBuilder(paramBuilder -> paramBuilder.setBytes(UUIDConverter.convert(worldUuid)))
                .delete().execute().thenApply(integer -> integer != 0);
    }

    @Override
    public CompletableFuture<List<BuildWorldData>> buildWorldDataList() {
        return buildWorldDataListStatementStage().emptyParams()
                .readRow(resultSet -> new BuildWorldDataImpl(
                        UUIDConverter.convert(resultSet.getBytes("worldUuid")),
                        resultSet.getString("worldName"),
                        resultSet.getString("gameType"),
                        UUIDConverter.convert(resultSet.getBytes("worldCreatorUuid")),
                        resultSet.getTimestamp("created").getTime()
                )).all();
    }

    @Override
    public CompletableFuture<List<BuildWorldData>> buildWorldDataListOfBuilder(UUID builderUuid) {
        return buildWorldDataListOfBuilderStatementStage()
                .paramsBuilder(paramBuilder -> paramBuilder.setBytes(UUIDConverter.convert(builderUuid)))
                .readRow(resultSet -> new BuildWorldDataImpl(
                        UUIDConverter.convert(resultSet.getBytes("worldUuid")),
                        resultSet.getString("worldName"),
                        resultSet.getString("gameType"),
                        UUIDConverter.convert(resultSet.getBytes("worldCreatorUuid")),
                        resultSet.getTimestamp("created").getTime())
                ).all();
    }
}
