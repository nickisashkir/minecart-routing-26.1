package ec.brooke.minecartrouting.feature;

import ec.brooke.minecartrouting.Utils;
import ec.brooke.minecartrouting.mixin.DisplayAccessor;
import ec.brooke.minecartrouting.mixin.TextDisplayAccessor;
import ec.brooke.minecartrouting.store.Filter;
import ec.brooke.minecartrouting.store.FilterAttachment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class Assigner {

    public static final String DISPLAY_TAG = "minecart_filter";

    public static void register() {
        UseBlockCallback.EVENT.register(Assigner::onUseBlock);
        AttackBlockCallback.EVENT.register(Assigner::onAttackBlock);
        PlayerBlockBreakEvents.AFTER.register(Assigner::onBlockBroken);
        ServerChunkEvents.CHUNK_LOAD.register(Assigner::onChunkLoad);
    }

    private static void onChunkLoad(ServerLevel level, net.minecraft.world.level.chunk.LevelChunk chunk, boolean isNew) {
        int minX = chunk.getPos().x() << 4;
        int minZ = chunk.getPos().z() << 4;
        AABB chunkBox = new AABB(minX, level.getMinY(), minZ, minX + 16, level.getMaxY(), minZ + 16);

        for (Display display : level.getEntities(EntityType.ITEM_DISPLAY, chunkBox, e -> e.entityTags().contains(DISPLAY_TAG))) {
            if (!level.getBlockState(display.blockPosition()).is(Blocks.DETECTOR_RAIL)) {
                display.discard();
            }
        }
        for (Display display : level.getEntities(EntityType.TEXT_DISPLAY, chunkBox, e -> e.entityTags().contains(DISPLAY_TAG))) {
            if (!level.getBlockState(display.blockPosition()).is(Blocks.DETECTOR_RAIL)) {
                display.discard();
            }
        }
    }

    private static void onBlockBroken(Level level, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
        if (!state.is(Blocks.DETECTOR_RAIL)) return;
        if (!(level instanceof ServerLevel sl)) return;
        if (FilterAttachment.get(level, pos) == null) return;
        updateFilter(sl, pos, null);
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.DETECTOR_RAIL)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        Filter current = FilterAttachment.get(level, pos);
        boolean sneaking = player.isShiftKeyDown();

        // Empty hand interactions
        if (held.isEmpty()) {
            if (current == null) return InteractionResult.PASS;

            if (sneaking) {
                // Shift + right-click empty hand: toggle whitelist/blacklist
                level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1f, 1.5f);
                Filter inverted = current.invert();
                updateFilter((ServerLevel) level, pos, inverted);
                player.sendOverlayMessage(Component.literal("Filter: " + inverted.displayText()));
            } else {
                // Right-click empty hand: show filter info
                player.sendOverlayMessage(Component.literal("Filter: " + current.displayText()));
            }
            return InteractionResult.SUCCESS;
        }

        if (sneaking) return InteractionResult.PASS;

        // Check if holding a ticket (color or direction)
        String ticketTag = Ticket.getTicket(held);
        if (ticketTag != null) {
            Filter updated = current == null
                    ? new Filter(List.of(ticketTag), true)
                    : current.withTag(ticketTag);
            if (updated == current) return InteractionResult.PASS;
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1f, 1f);
            updateFilter((ServerLevel) level, pos, updated);
            player.sendOverlayMessage(Component.literal("Filter: " + updated.displayText()));
            return InteractionResult.SUCCESS;
        }

        // Check if holding a dye
        DyeColor dye = Utils.ITEM_TO_DYE.get(held.getItem());
        if (dye != null) {
            String dyeTag = dye.name().toLowerCase();
            Filter updated = current == null
                    ? new Filter(List.of(dyeTag), true)
                    : current.withTag(dyeTag);
            if (updated == current) return InteractionResult.PASS;
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1f, 1f);
            updateFilter((ServerLevel) level, pos, updated);
            player.sendOverlayMessage(Component.literal("Filter: " + updated.displayText()));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private static InteractionResult onAttackBlock(Player player, Level level, InteractionHand hand, BlockPos pos, Direction direction) {
        if (level.isClientSide()) return InteractionResult.PASS;

        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.DETECTOR_RAIL)) return InteractionResult.PASS;

        Filter current = FilterAttachment.get(level, pos);
        if (current == null) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            // Shift + left-click: clear all filters
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1f, 1f);
            updateFilter((ServerLevel) level, pos, null);
            player.sendOverlayMessage(Component.literal("Filter cleared"));
            return InteractionResult.SUCCESS;
        } else {
            // Left-click: show filter info
            player.sendOverlayMessage(Component.literal("Filter: " + current.displayText()));
            return InteractionResult.SUCCESS;
        }
    }

    public static void updateFilter(ServerLevel level, BlockPos pos, Filter filter) {
        if (filter == null) FilterAttachment.remove(level, pos);
        else FilterAttachment.put(level, pos, filter);

        updateDisplay(level, pos, filter);
    }

    private static void updateDisplay(ServerLevel level, BlockPos pos, Filter filter) {
        // Remove any existing display entities (item or text)
        Vec3 center = Vec3.atCenterOf(pos);
        AABB box = new AABB(center.add(-1, -1, -1), center.add(1, 1, 1));

        for (Display display : level.getEntities(EntityType.TEXT_DISPLAY, box, e -> e.entityTags().contains(DISPLAY_TAG))) {
            display.discard();
        }
        for (Display display : level.getEntities(EntityType.ITEM_DISPLAY, box, e -> e.entityTags().contains(DISPLAY_TAG))) {
            display.discard();
        }

        if (filter == null) return;

        BlockState state = level.getBlockState(pos);
        RailShape shape = state.is(Blocks.DETECTOR_RAIL)
                ? state.getValue(DetectorRailBlock.SHAPE) : RailShape.NORTH_SOUTH;

        if (filter.tags().size() == 1) {
            // Single filter: use item display (concrete/glass/compass)
            Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
            display.setPos(center);
            display.addTag(DISPLAY_TAG);
            display.getSlot(0).set(getIndicatorItem(filter));
            ((DisplayAccessor) display).minecart_routing$setTransformation(Utils.shapeTransformation(shape));
            level.addFreshEntity(display);
        } else {
            // Multi filter: use text display showing "N | Red"
            Display.TextDisplay display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
            display.setPos(center);
            display.addTag(DISPLAY_TAG);
            ((TextDisplayAccessor) display).minecart_routing$setText(Component.literal(filter.shortText()));
            ((DisplayAccessor) display).minecart_routing$setTransformation(Utils.shapeTransformation(shape));
            level.addFreshEntity(display);
        }
    }

    private static ItemStack getIndicatorItem(Filter filter) {
        DyeColor dye = filter.singleDyeColor();
        if (dye != null) {
            return (filter.whitelist()
                    ? Utils.DYE_TO_CONCRETE.get(dye)
                    : Utils.DYE_TO_STAINED_GLASS.get(dye)).copy();
        }
        return new ItemStack(Items.COMPASS);
    }
}
