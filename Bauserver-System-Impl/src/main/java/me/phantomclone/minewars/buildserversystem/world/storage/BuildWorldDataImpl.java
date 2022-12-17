package me.phantomclone.minewars.buildserversystem.world.storage;

import me.phantomclone.minewars.buildserversystem.gametype.GameTyp;
import me.phantomclone.minewars.buildserversystem.gametype.GameTypRegistry;
import me.phantomclone.minewars.buildserversystem.skincache.SkinCache;
import me.phantomclone.minewars.buildserversystem.world.BuildWorld;
import me.phantomclone.minewars.buildserversystem.world.BuildWorldImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.World;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public record BuildWorldDataImpl(UUID worldUuid, String worldName, String gameType, UUID worldCreatorUuid, long created,
                                 boolean evaluate, UUID approvedHeadBuilder, long approvedTime,
                                 Atomic<CompletableFuture<BuildWorld>> atomicCompletableFuture)
        implements BuildWorldData {

    public BuildWorldDataImpl(UUID worldUuid, String worldName, String gameType, UUID worldCreatorUuid, long created,
                              boolean evaluate, UUID approvedHeadBuilder, long approvedTime) {
        this(worldUuid, worldName, gameType, worldCreatorUuid, created, evaluate, approvedHeadBuilder, approvedTime,
                new Atomic<>());
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

    @Override
    public Component[] loreComponent(SkinCache skinCache, GameTypRegistry gameTypRegistry) {
        final Component[] state;
         if (evaluate()) {
            state = new TextComponent[]{
                    Component.text("Warte auf Head Builder")
            };
        } else if (approvedTime() == 0 && approvedHeadBuilder() != null) {
            state = new TextComponent[] {
                    Component.text("BauWelt wurde von ")
                            .append(Component.text(skinCache.playerNameOfPlayerUuid(approvedHeadBuilder(), false)))
                            .append(Component.text(" abgelehnt"))
            };
        } else if (approvedTime() > 0 && approvedHeadBuilder() != null) {
            state = new TextComponent[]{
                    Component.text("BauWelt wurde von ")
                            .append(Component.text(skinCache.playerNameOfPlayerUuid(approvedHeadBuilder(), false)))
                            .append(Component.text(" genehmigt")),
                    Component.text("Genehmig am ").append(Component.text(DateFormat.getInstance()
                            .format(new Date(approvedTime()))))
            };
        } else{
            state = new TextComponent[] {
                    Component.text("Noch nicht zur Bewertung bereit")
            };
        }
        final TextComponent[] lore = new TextComponent[] {
                Component.empty(),
                Component.text("Welt Uuid: ").append(Component.text(worldUuid().toString())),
                Component.text("Game Modus: ").append(
                        gameTypRegistry.gameTypeList().stream()
                                .filter(gameTyp -> gameTyp.shortName().equals(gameType()))
                                .map(GameTyp::displayName)
                                .findFirst().orElse(Component.text("Not found!"))
                ),
                Component.text("Welt Ersteller: ").append(Component.text(
                        skinCache.playerNameOfPlayerUuid(worldCreatorUuid(), false))),
                Component.text("Erstellt am ").append(Component.text(DateFormat.getInstance()
                        .format(new Date(created()))))
        };
        return Stream.of(lore, state).flatMap(Stream::of)
                .toArray(Component[]::new);
    }

    private static class Atomic<T> {
        private T t;
    }

}
