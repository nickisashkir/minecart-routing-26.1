package ec.brooke.minecartrouting.store;

import com.mojang.serialization.Codec;
import ec.brooke.minecartrouting.MinecartRoutingMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashMap;
import java.util.Map;

public class FilterAttachment {

    private static final Codec<BlockPos> BLOCK_POS_STRING_CODEC = Codec.STRING.xmap(
            s -> {
                String[] p = s.split(",");
                return new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
            },
            pos -> pos.getX() + "," + pos.getY() + "," + pos.getZ()
    );

    public static final Codec<Map<BlockPos, Filter>> MAP_CODEC =
            Codec.unboundedMap(BLOCK_POS_STRING_CODEC, Filter.CODEC);

    public static AttachmentType<Map<BlockPos, Filter>> FILTERS;

    public static void register() {
        FILTERS = AttachmentRegistry.<Map<BlockPos, Filter>>builder()
                .persistent(MAP_CODEC)
                .initializer(HashMap::new)
                .buildAndRegister(MinecartRoutingMod.id("filters"));
    }

    private static LevelChunk chunkAt(Level level, BlockPos pos) {
        return level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static Filter get(Level level, BlockPos pos) {
        LevelChunk chunk = chunkAt(level, pos);
        Map<BlockPos, Filter> map = chunk.getAttached(FILTERS);
        return map == null ? null : map.get(pos);
    }

    public static void put(Level level, BlockPos pos, Filter filter) {
        LevelChunk chunk = chunkAt(level, pos);
        Map<BlockPos, Filter> map = chunk.getAttachedOrCreate(FILTERS);
        map.put(pos.immutable(), filter);
        chunk.setAttached(FILTERS, map);
        chunk.markUnsaved();
    }

    public static void remove(Level level, BlockPos pos) {
        LevelChunk chunk = chunkAt(level, pos);
        Map<BlockPos, Filter> map = chunk.getAttached(FILTERS);
        if (map == null) return;
        if (map.remove(pos) == null) return;
        chunk.setAttached(FILTERS, map.isEmpty() ? null : map);
        chunk.markUnsaved();
    }
}
