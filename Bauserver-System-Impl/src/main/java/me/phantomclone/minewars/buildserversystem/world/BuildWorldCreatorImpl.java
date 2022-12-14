package me.phantomclone.minewars.buildserversystem.world;

import me.phantomclone.minewars.buildserversystem.world.worldSetting.WorldSetting;
import me.phantomclone.minewars.buildserversystem.world.worldSetting.WorldType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BuildWorldCreatorImpl implements BuildWorldCreator {

    private WorldType worldType;
    private String gameType;
    private final Set<WorldSetting> worldSettingList = new HashSet<>();

    private String worldName;

    private final BuildWorldHandler buildWorldHandler;

    public BuildWorldCreatorImpl(BuildWorldHandler buildWorldHandler) {
        this.buildWorldHandler = buildWorldHandler;
    }

    @Override
    public BuildWorldCreator applyWorldType(WorldType worldType) {
        this.worldType = worldType;
        return this;
    }

    @Override
    public WorldType getWorldType() {
        return this.worldType;
    }

    @Override
    public BuildWorldCreator applyGameType(String gameType) {
        this.gameType = gameType;
        return this;
    }

    @Override
    public String getGameType() {
        return gameType;
    }

    @Override
    public BuildWorldCreator applySetting(WorldSetting worldSetting) {
        this.worldSettingList.add(worldSetting);
        return this;
    }

    @Override
    public Set<WorldSetting> getWorldSettingSet() {
        return worldSettingList;
    }

    @Override
    public BuildWorld createBuildWorld(String worldName, UUID creatorUuid) {
        this.worldName = worldName;
        return buildWorldHandler.applyWorldCreator(this, creatorUuid);
    }

    @Override
    public String getWorldName() {
        return this.worldName;
    }
}
