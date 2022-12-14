package me.phantomclone.minewars.buildserversystem.world.storage;

import de.chojo.sqlutil.conversion.UUIDConverter;
import de.chojo.sqlutil.wrapper.QueryBuilder;
import de.chojo.sqlutil.wrapper.stage.StatementStage;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record BuilderStorageImpl(StatementStage<?> createTableStatementStage,
                                 StatementStage<?> insertBuilderStatementStage,
                                 StatementStage<?> removeBuilderStatementStage,
                                 StatementStage<UUID> worldUuidOfBuilderStatementStage,
                                 StatementStage<UUID> builderUuidOfWorldStatementStage
                                 ) implements BuilderStorage {

    public BuilderStorageImpl(DataSource dataSource) {
        this(
                QueryBuilder.builder(dataSource).defaultConfig()
                        .query("CREATE TABLE IF NOT EXISTS Builder(builderUuid BINARY(16) NOT NULL, " +
                                "worldUuid BINARY(16) NOT NULL, PRIMARY KEY (builderUuid, worldUuid), " +
                                "FOREIGN KEY (worldUuid) REFERENCES BuildWorld)"),
                QueryBuilder.builder(dataSource).defaultConfig()
                        .query("INSERT INTO Builder(builderUuid, worldUuid) VALUES(?, ?)"),
                QueryBuilder.builder(dataSource).defaultConfig()
                        .query("DELETE FROM Builder WHERE builderUuid=? AND worldUuid=?"),
                QueryBuilder.builder(dataSource, UUID.class).defaultConfig()
                        .query("SELECT worldUuid FROM Builder WHERE builderUuid=?"),
                QueryBuilder.builder(dataSource, UUID.class).defaultConfig()
                        .query("SELECT builderUuid FROM Builder WHERE worldUuid=?")
                );
    }

    @Override
    public CompletableFuture<Boolean> createTable() {
        return createTableStatementStage().emptyParams().update().execute().thenApply(integer -> integer != 0);
    }

    @Override
    public CompletableFuture<Boolean> insertBuilder(UUID builderUuid, UUID worldUuid) {
        return insertBuilderStatementStage()
                .paramsBuilder(paramBuilder -> paramBuilder.setBytes(UUIDConverter.convert(builderUuid))
                        .setBytes(UUIDConverter.convert(worldUuid))).insert().execute()
                .thenApply(integer -> integer != 0);
    }

    @Override
    public CompletableFuture<Boolean> removeBuilder(UUID builderUuid, UUID worldUuid) {
        return removeBuilderStatementStage()
                .paramsBuilder(paramBuilder -> paramBuilder.setBytes(UUIDConverter.convert(builderUuid))
                        .setBytes(UUIDConverter.convert(worldUuid))).insert().execute()
                .thenApply(integer -> integer != 0);
    }

    @Override
    public CompletableFuture<List<UUID>> worldUuidOfBuilder(UUID builderUuid) {
        return worldUuidOfBuilderStatementStage()
                .paramsBuilder(paramBuilder -> paramBuilder.setBytes(UUIDConverter.convert(builderUuid)))
                .readRow(resultSet -> UUIDConverter.convert(resultSet.getBytes("worldUuid")))
                .all();
    }

    @Override
    public CompletableFuture<List<UUID>> builderUuidOfWorld(UUID worldUuid) {
        return worldUuidOfBuilderStatementStage()
                .paramsBuilder(paramBuilder -> paramBuilder.setBytes(UUIDConverter.convert(worldUuid)))
                .readRow(resultSet -> UUIDConverter.convert(resultSet.getBytes("builderUuid")))
                .all();
    }
}
