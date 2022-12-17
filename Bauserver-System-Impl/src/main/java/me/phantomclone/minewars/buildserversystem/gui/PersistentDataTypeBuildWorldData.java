package me.phantomclone.minewars.buildserversystem.gui;

import de.chojo.sqlutil.conversion.UUIDConverter;
import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldData;
import me.phantomclone.minewars.buildserversystem.world.storage.BuildWorldDataImpl;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PersistentDataTypeBuildWorldData implements PersistentDataType<byte[], BuildWorldData> {
    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<BuildWorldData> getComplexType() {
        return BuildWorldData.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(@NotNull BuildWorldData buildWorldData, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        final byte[] nameBytes = buildWorldData.worldName().getBytes(StandardCharsets.UTF_8);
        final byte[] typeByte = buildWorldData.gameType().getBytes(StandardCharsets.UTF_8);
        final ByteBuffer byteBuffer = ByteBuffer.allocate(16 + 16 + 4 + nameBytes.length + 4 + typeByte.length + 8 +
                1 + (buildWorldData.approvedHeadBuilder() != null ? 16 + 8 : 0));
        byteBuffer.put(UUIDConverter.convert(buildWorldData.worldUuid()));
        byteBuffer.put(UUIDConverter.convert(buildWorldData.worldCreatorUuid()));
        byteBuffer.putInt(nameBytes.length);
        byteBuffer.put(nameBytes);
        byteBuffer.putInt(typeByte.length);
        byteBuffer.put(typeByte);
        byteBuffer.putLong(buildWorldData.created());
        byteBuffer.put((byte) (buildWorldData.evaluate() ? 0b1 : 0b0));
        if (buildWorldData.approvedHeadBuilder() != null) {
            byteBuffer.put(UUIDConverter.convert(buildWorldData.approvedHeadBuilder()));
            byteBuffer.putLong(buildWorldData.approvedTime());
        }
        return byteBuffer.array();
    }

    @Override
    public @NotNull BuildWorldDataImpl fromPrimitive(byte @NotNull [] bytes, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        final byte[] uuidBuffer = new byte[16];
        byteBuffer.get(uuidBuffer);
        final UUID worldUuid = UUIDConverter.convert(uuidBuffer);
        byteBuffer.get(uuidBuffer);
        final UUID worldCreator = UUIDConverter.convert(uuidBuffer);
        final int nameLength = byteBuffer.getInt();
        final byte[] nameByte = new byte[nameLength];
        byteBuffer.get(nameByte);
        final String worldName = new String(nameByte, StandardCharsets.UTF_8);
        final int typeLength = byteBuffer.getInt();
        final byte[] typeByte = new byte[typeLength];
        byteBuffer.get(typeByte);
        final String gameType = new String(typeByte, StandardCharsets.UTF_8);
        final long created = byteBuffer.getLong();
        final boolean evaluate = byteBuffer.get() != 0;
        final UUID approvedHeadBuilder;
        final long approvedTime;
        if (byteBuffer.hasRemaining()) {
            byteBuffer.get(uuidBuffer);
            approvedHeadBuilder = UUIDConverter.convert(uuidBuffer);
            approvedTime = byteBuffer.getLong();
        } else {
            approvedHeadBuilder = null;
            approvedTime = 0;
        }
        return new BuildWorldDataImpl(worldUuid, worldName, gameType, worldCreator, created,
                evaluate, approvedHeadBuilder, approvedTime);
    }
}
