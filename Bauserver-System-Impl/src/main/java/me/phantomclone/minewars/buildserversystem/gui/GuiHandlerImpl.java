package me.phantomclone.minewars.buildserversystem.gui;

import me.phantomclone.minewars.buildserversystem.BuildServerPlugin;
import me.phantomclone.minewars.buildserversystem.skincache.SkinCache;
import me.phantomclone.minewars.buildserversystem.world.storage.BuilderStorage;

public record GuiHandlerImpl(
        AllBuildWorldGui allBuildWorldGui,
        BuilderGui builderGui,
        BuildSystemGui buildSystemGui,
        WorldSettingsGui worldSettingsGui,
        CreateNewWorldGui createNewWorldGui
) implements GuiHandler{

    public GuiHandlerImpl(BuildServerPlugin buildServerPlugin, BuilderStorage builderStorage, SkinCache skinCache) {
        this(
                buildServerPlugin,
                new AllBuildWorldGuiImpl(buildServerPlugin, skinCache),
                new BuilderGuiImpl(buildServerPlugin, builderStorage, skinCache),
                new WorldSettingsGuiImpl(),
                new CreateNewWorldGuiImpl(buildServerPlugin)
        );
    }

    private GuiHandlerImpl(BuildServerPlugin buildServerPlugin, AllBuildWorldGui allBuildWorldGui,
                          BuilderGui builderGui,
                          WorldSettingsGui worldSettingsGui,
                          CreateNewWorldGui createNewWorldGui) {
        this(
                allBuildWorldGui, builderGui,
                new BuildSystemGuiImpl(buildServerPlugin, allBuildWorldGui, createNewWorldGui), worldSettingsGui,
                createNewWorldGui
        );
    }
}
