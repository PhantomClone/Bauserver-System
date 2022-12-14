package me.phantomclone.minewars.buildserversystem.world.worldSetting;

import org.bukkit.GameRule;
import org.bukkit.World;

import java.util.function.Consumer;

public enum WorldSetting {

    TIME_DOES_NOT_PASS(11, world -> world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)),
    WEATHER_DOES_NOT_CHANGE(12, world -> world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)),
    MOBS_DOES_NOT_SPAWN(13, world -> world.setGameRule(GameRule.DO_MOB_SPAWNING, false)),
    FIRE_DOES_NOT_SPREAD(14, world -> world.setGameRule(GameRule.DO_FIRE_TICK, false)),
    PLAYERS_CAN_NOT_TAKE_DAMAGE(15, WorldSetting::applyNoDamage);

    private final int slotPos;
    private final Consumer<World> applyWorldRule;

    WorldSetting(int slotPos, Consumer<World> applyWorldRule) {
        this.slotPos = slotPos;
        this.applyWorldRule = applyWorldRule;
    }

    public int slotPos() {
        return slotPos;
    }

    public void applyWorldRule(World world) {
        this.applyWorldRule.accept(world);
    }

    private static void applyNoDamage(World world) {
        world.setGameRule(GameRule.DROWNING_DAMAGE, false);
        world.setGameRule(GameRule.FALL_DAMAGE, false);
        world.setGameRule(GameRule.FIRE_DAMAGE, false);
    }
}
