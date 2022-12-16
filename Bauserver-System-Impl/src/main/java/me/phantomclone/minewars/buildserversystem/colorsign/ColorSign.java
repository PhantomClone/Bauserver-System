package me.phantomclone.minewars.buildserversystem.colorsign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ColorSign implements Listener {

    private final JavaPlugin javaPlugin;
    private final List<Player> uuidList = new ArrayList<>();

    public ColorSign(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        setUpProtocol();
    }

    @EventHandler
    public void onSignChange(SignChangeEvent signChangeEvent) {
        signChangeEvent.lines().replaceAll(component -> LegacyComponentSerializer.legacyAmpersand()
                .deserialize(LegacyComponentSerializer.legacyAmpersand().serialize(component)));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent playerInteractEvent){
        final Block clickedBlock = playerInteractEvent.getClickedBlock();
        final Player player = playerInteractEvent.getPlayer();
        if (player.isSneaking() && clickedBlock != null && clickedBlock.getState() instanceof Sign sign) {
            synchronized (this.uuidList) {
                if (this.uuidList.contains(player))
                    return;
            }
            sign.lines().replaceAll(component -> Component.text(
                    LegacyComponentSerializer.legacyAmpersand().serialize(component)));
            sign.update();

            javaPlugin.getServer().getScheduler().runTaskLater(this.javaPlugin, () -> {

                final PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.OPEN_SIGN_EDITOR);

                packetContainer.getBlockPositionModifier().write(0, new BlockPosition(sign.getX(), sign.getY(), sign.getZ()));
                synchronized (this.uuidList) {
                    this.uuidList.add(player);
                }
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(playerInteractEvent.getPlayer(),
                            packetContainer
                    );
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    synchronized (this.uuidList) {
                        this.uuidList.remove(player);
                    }
                }
            }, 10); //WTF... why do u have to wait so long... there is something wrong with the server and or client, that sign.update() takes so long
        }
    }

    private void setUpProtocol() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this.javaPlugin, PacketType.Play.Client.UPDATE_SIGN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                synchronized (uuidList) {
                    if (uuidList.removeIf(player -> player.equals(event.getPlayer()))) {
                        event.setCancelled(true);
                        final BlockPosition blockPosition = event.getPacket().getBlockPositionModifier().getValues().get(0);
                        final String[] newLines = event.getPacket().getStringArrays().getValues().get(0);
                        javaPlugin.getServer().getScheduler().runTask(javaPlugin, () -> {
                            Block blockAt = event.getPlayer().getWorld().getBlockAt(blockPosition.toLocation(event.getPlayer().getWorld()));
                            if (blockAt.getState() instanceof Sign sign) {
                                for (int i = 0; i < newLines.length; i++) {
                                    sign.line(i, LegacyComponentSerializer.legacyAmpersand().deserialize(newLines[i]));
                                }
                                sign.update();
                            }
                        });
                    }
                }
            }
        });
    }
}
