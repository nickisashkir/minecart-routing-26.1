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

    public static final Codec<Map<BlockPos, DyeFilter>> MAP_CODEC =
            Codec.unboundedMap(BlockPos.CODEC, DyeFilter.CODEC);

    public static AttachmentType<Map<BlockPos, DyeFilter>> FILTERS;

    public static void register() {
        FILTERS = AttachmentRegistry.<Map<BlockPos, DyeFilter>>builder()
                .persistent(MAP_CODEC)
                .initializer(HashMap::new)
                .buildAndRegister(MinecartRoutingMod.id("filters"));
    }

    private static LevelChunk chunkAt(Level level, BlockPos pos) {
        return level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static DyeFilter get(Level level, BlockPos pos) {
        LevelChunk chunk = chunkAt(level, pos);
        Map<BlockPos, DyeFilter> map = chunk.getAttached(FILTERS);
        return map == null ? null : map.get(pos);
    }

    public static void put(Level level, BlockPos pos, DyeFilter filter) {
        LevelChunk chunk = chunkAt(level, pos);
        Map<BlockPos, DyeFilter> map = chunk.getAttachedOrCreate(FILTERS);
        map.put(pos.immutable(), filter);
        chunk.setAttached(FILTERS, map);
        chunk.markUnsaved();
    }

    public static void remove(Level level, BlockPos pos) {
        LevelChunk chunk = chunkAt(level, pos);
        Map<BlockPos, DyeFilter> map = chunk.getAttached(FILTERS);
        if (map == null) return;
        if (map.remove(pos) == null) return;
        chunk.setAttached(FILTERS, map.isEmpty() ? null : map);
        chunk.markUnsaved();
    }
}
