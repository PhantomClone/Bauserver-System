package me.phantomclone.minewars.buildserversystem.skincache;

import java.util.UUID;

public interface SkinCache {

    UUID playerUuidOfPlayerName(String playerName, boolean fromMojang);
    String playerNameOfPlayerUuid(UUID playerUuid, boolean fromSessionServer);
    String skinValueOfPlayerUuid(UUID playerUuid, boolean fromSessionServer);

}
