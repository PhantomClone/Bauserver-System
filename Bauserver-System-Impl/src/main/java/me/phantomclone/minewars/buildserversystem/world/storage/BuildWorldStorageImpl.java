package me.phantomclone.minewars.buildserversystem.world.storage;

import de.chojo.sqlutil.conversion.UUIDConverter;
import de.chojo.sqlutil.wrapper.QueryBuilder;
import de.chojo.sqlutil.wrapper.stage.StatementStage;
import me.phantomclone.minewars.buildserversystem.world.BuildWorld;
import org.apache.commons.io.FileUtils;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

//TODO DELETE -> change to chunk save methode - see TestWorldStoreProject!
public record BuildWorldStorageImpl(JavaPlugin javaPlugin,
                                    StatementStage<?> createTableStatementStage,
                                    StatementStage<Pair> loadBuildWorldStatementStage,
                                    StatementStage<?> saveBuildWorldStatementStage,

                                    StatementStage<Integer> testTimes,
                                    StatementStage<InputStream> testInputStream

) implements BuildWorldStorage {

    public BuildWorldStorageImpl(JavaPlugin javaPlugin, DataSource dataSource) {
        this(
                javaPlugin,
                QueryBuilder.builder(dataSource).defaultConfig()
                        .query("CREATE TABLE IF NOT EXISTS WorldData(worldUuid BINARY(16) NOT NULL, times INT NOT NULL, worldData LONGBLOB NOT NULL, " +
                                "PRIMARY KEY (worldUuid, times), FOREIGN KEY (worldUuid) REFERENCES BuildWorld)"),
                QueryBuilder.builder(dataSource, Pair.class).defaultConfig()
                        .query("SELECT worldData, times FROM WorldData WHERE worldUuid = ?"),
                QueryBuilder.builder(dataSource).defaultConfig()
                        .query("INSERT INTO WorldData(worldUuid, times, worldData) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE worldData=VALUES(worldData)")
        ,QueryBuilder.builder(dataSource, Integer.class).defaultConfig()
                        .query("SELECT times FROM WorldData WHERE worldUuid = ?"),
        QueryBuilder.builder(dataSource, InputStream.class).defaultConfig()
                .query("SELECT worldData FROM WorldData WHERE worldUuid = ? AND times = ?")
        );
    }

    @Override
    public CompletableFuture<Boolean> createTable() {
        return createTableStatementStage().emptyParams().update().execute().thenApply(integer -> integer != 0);
    }

    private void handleLoadBuildList(Iterator<Integer> iterator, BuildWorldData buildWorldData, List<UUID> builderList,
                                     CompletableFuture<BuildWorld> completableFuture) {

    }

    @Override
    public CompletableFuture<Boolean> worldExists(UUID worldUuid) {
        return null;
    }

    @Override
    public CompletableFuture<BuildWorld> loadBuildWorld(BuildWorldData buildWorldData, List<UUID> builderList) {
        CompletableFuture<BuildWorld> completableFuture = new CompletableFuture<>();

        testTimes().paramsBuilder(paramBuilder -> paramBuilder.setBytes(UUIDConverter.convert(buildWorldData.worldUuid())))
                .readRow(resultSet -> resultSet.getInt("times")).all().whenComplete((integerList, throwable) ->
                    handleLoadBuildList(integerList.iterator(), buildWorldData, builderList, completableFuture)
                );
        return  completableFuture;
        /*


        loadBuildWorldStatementStage().paramsBuilder(paramBuilder -> paramBuilder.setBytes(UUIDConverter.convert(buildWorldData.worldUuid())))
                .readRow(resultSet -> new Pair(resultSet.getInt("times"),
                        resultSet.getBinaryStream("worldData"), UUID.randomUUID())).all(command ->
                    javaPlugin().getServer().getScheduler().runTaskAsynchronously(javaPlugin(), command)
                ).whenComplete((pairList, throwable) ->
                    javaPlugin().getServer().getScheduler().runTaskAsynchronously(javaPlugin, () -> {
                        if (pairList.isEmpty()) {
                            completableFuture.complete(null);
                        } else
                            this.handleIncomingInputStream(pairList, buildWorldData, builderList)
                                    .whenComplete((buildWorld, throwable1) -> completableFuture.complete(buildWorld));
                    })
                );
        return completableFuture;
         */
    }

    private CompletableFuture<BuildWorld> handleIncomingInputStream(List<Pair> pairList,
                                                                    BuildWorldData buildWorldData, List<UUID> builderList) {
        final CompletableFuture<BuildWorld> completableFuture = new CompletableFuture<>();
        final List<InputStream> inputStreamList = new ArrayList<>();
        for (int i = 0; i < pairList.size(); i++) {
            pairList.stream().sorted(Comparator.comparingInt(Pair::integer))
                    .forEach(pair -> inputStreamList.add(pair.inputStream()));
        }

        System.out.println(inputStreamList.size());

        try (InputStream inputStream = new SequenceInputStream(Collections.enumeration(inputStreamList))) {
            final File worldContainer = new File(javaPlugin().getServer().getWorldContainer(), buildWorldData.worldUuid().toString());
            if (worldContainer.exists()) {
                try {
                    FileUtils.deleteDirectory(worldContainer);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    return CompletableFuture.completedFuture(null);
                }
            } worldContainer.mkdirs();
            try(ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                ZipEntry entry;
                byte[] buffer = new byte[1024];
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    File file = new File(worldContainer, entry.getName());
                    if (entry.isDirectory()) {
                        file.mkdirs();
                        continue;
                    }
                    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                        int n;
                        while ((n = zipInputStream.read(buffer)) != -1) {
                            out.write(buffer, 0, n);
                        }
                        out.flush();
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return CompletableFuture.completedFuture(null);
            }
            javaPlugin().getServer().getScheduler().runTask(javaPlugin(), () ->
                    completableFuture.complete(
                            buildWorldData.toBuildWorld(builderList, new WorldCreator(worldContainer.getName()).createWorld()))
            );
        } catch (IOException ioException) {
            ioException.printStackTrace();
            completableFuture.complete(null);
        }
        return completableFuture;
    }

    private void again(Iterator<File> iterator, byte[] worldUuid, AtomicInteger atomicInteger, CompletableFuture<Boolean> completableFuture) {
        System.out.println("First push");
        push(worldUuid, atomicInteger, iterator.next(), inputStreamToClose -> {
            try {
                inputStreamToClose.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Push " + atomicInteger.get());
            if (iterator.hasNext())
                again(iterator, worldUuid, atomicInteger, completableFuture);
             else completableFuture.complete(true);
        });
    }

    @Override
    public CompletableFuture<Boolean> saveBuildWorld(File worldFolder) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        javaPlugin().getServer().getScheduler().runTaskAsynchronously(javaPlugin(), () -> {
            try {
                AtomicInteger atomicInteger = new AtomicInteger(0);
                List<File> inputStreamFromWorld = getInputStreamFromWorld(worldFolder);
                Iterator<File> iterator = inputStreamFromWorld.iterator();
                byte[] worldUuid = UUIDConverter.convert(UUID.fromString(worldFolder.getName()));
                again(iterator, worldUuid, atomicInteger, completableFuture);
            } catch (SQLException exception) {
                exception.printStackTrace();
                completableFuture.complete(false);
            }
        });
        return completableFuture;
    }

    private void push(byte[] worldUuid, AtomicInteger atomicInteger, File file,
                                            Consumer<InputStream> inputStreamConsumer) {
        try {
            InputStream inputStream = new FileInputStream(file);
            System.out.printf("File %s \n Times %d", file.getName(), atomicInteger.get());
            saveBuildWorldStatementStage().paramsBuilder(paramBuilder -> paramBuilder
                            .setBytes(worldUuid)
                            .setInt(atomicInteger.getAndIncrement())
                            .setBinaryStream(inputStream))
                    .insert().execute(command -> javaPlugin().getServer().getScheduler().runTaskAsynchronously(javaPlugin(), command))
                    .whenComplete((integer, throwable) -> inputStreamConsumer.accept(inputStream));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private List<File> getInputStreamFromWorld(File worldFolder) throws SQLException {
        javaPlugin().getDataFolder().mkdirs();
        final File tempFile = new File(javaPlugin().getDataFolder(), UUID.randomUUID().toString());
        try {
            tempFile.createNewFile();
        } catch (IOException e) {
            throw new SQLException(e);
        }
        try (
                final FileOutputStream fos = new FileOutputStream(tempFile);
                final ZipOutputStream zos = new ZipOutputStream(fos)
        ) {
            Files.walkFileTree(worldFolder.toPath(), new SimpleFileVisitor<>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toFile().getName().endsWith(".lock"))
                        return FileVisitResult.CONTINUE;
                    zos.putNextEntry(new ZipEntry(worldFolder.toPath().relativize(file).toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    zos.putNextEntry(new ZipEntry(worldFolder.toPath().relativize(dir) + "/"));
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
            return zipToInputStream(tempFile);
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    private List<File> zipToInputStream(File zipFile) throws IOException {
        long size = Files.size(zipFile.toPath());
        if (size <= 500_000_000L)
            return List.of(zipFile);

        final List<File> fileList = new ArrayList<>();
        try (InputStream fileInputStream = new FileInputStream(zipFile)) {
            int bytesRead;
            byte[] buffer = new byte[1024];

            long maxFileSize = 500_000_000L;
            File file = new File(javaPlugin().getDataFolder(), UUID.randomUUID().toString());
            file.createNewFile();
            fileList.add(file);
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(bytesRead);
                maxFileSize -= bytesRead;
                System.out.println(maxFileSize);
                if (maxFileSize <= 0) {
                    fileOutputStream.close();
                    maxFileSize = 500_000_000L;
                    file = new File(javaPlugin().getDataFolder(), UUID.randomUUID().toString());
                    file.createNewFile();
                    fileList.add(file);
                    fileOutputStream = new FileOutputStream(file);
                }
            }
            fileOutputStream.close();
        }
        return fileList;
    }

    private record Pair(Integer integer, InputStream inputStream, UUID uuid) {

    }

}
