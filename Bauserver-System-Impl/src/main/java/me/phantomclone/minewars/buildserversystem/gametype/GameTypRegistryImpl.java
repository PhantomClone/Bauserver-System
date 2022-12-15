package me.phantomclone.minewars.buildserversystem.gametype;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record GameTypRegistryImpl(int inventorySize, List<GameTyp> gameTypeList) implements GameTypRegistry {

    private static GameTyp gameTypFromConfiguration(Configuration configuration, String gameTypeString) throws Exception {
        final String displayNameFormat = String.format("Modus.%s.Displayname", gameTypeString);
        final String displayColorFormat = String.format("Modus.%s.Displaycolors", gameTypeString);
        final String slotFormat = String.format("Modus.%s.Slot", gameTypeString);
        final String materialFormat = String.format("Modus.%s.Material", gameTypeString);
        final String shortNameFormat = String.format("Modus.%s.ShortName", gameTypeString);

        if (!configuration.isList(displayNameFormat))
            throw new Exception(String.format("Displayname is not in (String)List format (%s)!", gameTypeString));
        if (!configuration.isList(displayColorFormat))
            throw new Exception(String.format("Displaycolors is not in (Integer)List format (%s)!", gameTypeString));
        if (!configuration.isInt(slotFormat))
            throw new Exception(String.format("Slot is not an integer (%s)!", gameTypeString));
        if (!configuration.isString(materialFormat))
            throw new Exception(String.format("Material is not a string (%s)!", gameTypeString));
        if (!configuration.isString(shortNameFormat))
            throw new Exception(String.format("ShortName is not a string (%s)!", gameTypeString));

        final List<String> displayNames = configuration.getStringList(displayNameFormat);
        final List<Integer> displayColors = configuration.getIntegerList(displayColorFormat);
        final int slot = configuration.getInt(slotFormat);
        final Material material = Material.getMaterial(materialFormat);
        final String shortName = configuration.getString(shortNameFormat);

        if (material == null)
            throw new Exception(String.format("Material is not found (%s)!", gameTypeString));

        Component component = Component.empty();
        for (int i = 0; i < displayNames.size(); i++) {
            component = component.append(
                    Component.text(displayNames.get(i)).color(TextColor.color(TextColor.color(displayColors.get(i)))
                    )
            );
        }
        return new GameTypImpl(component, shortName, slot, material);
    }

    public static GameTypRegistryImpl loadFromConfiguration(Configuration configuration) throws Exception {
        if (!configuration.contains("InventorySize"))
            throw new Exception("No InventorySize found!");
        if (!configuration.isInt("InventorySize"))
            throw new Exception("InventorySize is not an integer!");
        final int inventorySize = configuration.getInt("InventorySize");
        if (!configuration.isConfigurationSection("Modus"))
            throw new Exception("No Modus ConfigurationSection found!");
        final ConfigurationSection configurationSection = configuration.getConfigurationSection("Modus");
        assert configurationSection != null;
        final Set<String> gameTypeSet = configurationSection.getKeys(false);
        final ArrayList<GameTyp> gameTypeList = new ArrayList<>();
        for (String gameTypeString : gameTypeSet) {
            gameTypeList.add(gameTypFromConfiguration(configuration, gameTypeString));
        }
        return new GameTypRegistryImpl(inventorySize, List.copyOf(gameTypeList));
    }

}
