package me.phantomclone.minewars.buildserversystem.world.storage;

import me.phantomclone.minewars.buildserversystem.world.BuildWorld;
import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BuildWorldStorageHardDrive implements BuildWorldStorage {

    private final JavaPlugin javaPlugin;

    public BuildWorldStorageHardDrive(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    @Override
    public CompletableFuture<Boolean> createTable() {
        return CompletableFuture.completedFuture(javaPlugin.getDataFolder().mkdirs());
    }

    @Override
    public CompletableFuture<Boolean> worldExists(UUID worldUuid) {
        return CompletableFuture.completedFuture(new File(javaPlugin.getDataFolder(), String.format("%s.zip", worldUuid))
                .exists());
    }

    @Override
    public CompletableFuture<BuildWorld> loadBuildWorld(BuildWorldData buildWorldData, List<UUID> builderList) {
        final CompletableFuture<BuildWorld> completableFuture = new CompletableFuture<>();
        javaPlugin.getServer().getScheduler().runTaskAsynchronously(javaPlugin, () -> {
            File worldFolder = new File(javaPlugin.getServer().getWorldContainer(), buildWorldData.worldUuid().toString());
            if (worldFolder.exists()) {
                try {
                    FileUtils.deleteDirectory(worldFolder);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    completableFuture.complete(null);
                    return;
                }
                extractZip(new File(javaPlugin.getDataFolder(), String.format("%s.zip", worldFolder.getName())));
            }
        });
        return completableFuture;
    }

    private void extractZip(File zipFile) {
        int length = zipFile.getName().length();
        final String worldName = zipFile.getName().substring(length - 4, length);
        try (FileInputStream fileInputStream = new FileInputStream(zipFile);
             ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File file = new File(worldName, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                    continue;
                }
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                int n;
                while ((n = zipInputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
                out.flush();
                out.close();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    @Override
    public CompletableFuture<Boolean> saveBuildWorld(File worldFolder) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        javaPlugin.getServer().getScheduler().runTaskAsynchronously(javaPlugin, () -> createZipFile(
                worldFolder, new File(javaPlugin.getDataFolder(), String.format("%s.zip", worldFolder.getName())),
                completableFuture
        ));
        return completableFuture;
    }

    private void createZipFile(File worldFolder, File zipFile, CompletableFuture<Boolean> completableFuture) {
        try {
            if (!zipFile.createNewFile())
                throw new IOException("Cant create Zip file");
        } catch (IOException ioException) {
            ioException.printStackTrace();
            completableFuture.complete(false);
            return;
        }
        try (
                final FileOutputStream fos = new FileOutputStream(zipFile);
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
        } catch (IOException ioException) {
            ioException.printStackTrace();
            completableFuture.complete(false);
            return;
        }
        completableFuture.complete(true);
    }

}
