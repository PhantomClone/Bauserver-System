package me.phantomclone.minewars.buildserversystem.world;

import me.phantomclone.minewars.buildserversystem.world.worldSetting.WorldSetting;
import me.phantomclone.minewars.buildserversystem.world.worldSetting.WorldType;

import java.util.Set;
import java.util.UUID;

public interface BuildWorldCreator {

    BuildWorldCreator applyWorldType(WorldType worldType);
    WorldType getWorldType();
    BuildWorldCreator applyGameType(String gameType);

    String getGameType();
    BuildWorldCreator applySetting(WorldSetting worldSetting);
    Set<WorldSetting> getWorldSettingSet();
    BuildWorld createBuildWorld(String worldName, UUID creatorUuid);

    String getWorldName();

}
