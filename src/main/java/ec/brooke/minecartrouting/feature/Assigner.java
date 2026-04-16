package ec.brooke.minecartrouting.feature;

import ec.brooke.minecartrouting.Utils;
import ec.brooke.minecartrouting.mixin.DisplayAccessor;
import ec.brooke.minecartrouting.store.DyeFilter;
import ec.brooke.minecartrouting.store.FilterAttachment;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.DETECTOR_RAIL)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        DyeFilter current = FilterAttachment.get(level, pos);

        if (held.isEmpty()) {
            if (current == null) return InteractionResult.PASS;
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1f, 1.5f);
            update((ServerLevel) level, pos, current.invert());
            return InteractionResult.SUCCESS;
        }

        DyeColor dye = Utils.ITEM_TO_DYE.get(held.getItem());
        if (dye == null) return InteractionResult.PASS;
        if (current != null && current.color() == dye && current.whitelist()) return InteractionResult.PASS;

        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1f, 1f);
        update((ServerLevel) level, pos, new DyeFilter(dye, true));
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult onAttackBlock(Player player, Level level, InteractionHand hand, BlockPos pos, Direction direction) {
        if (level.isClientSide()) return InteractionResult.PASS;

        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.DETECTOR_RAIL)) return InteractionResult.PASS;

        DyeFilter current = FilterAttachment.get(level, pos);
        if (current == null) return InteractionResult.PASS;

        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1f, 1f);
        update((ServerLevel) level, pos, null);
        return InteractionResult.SUCCESS;
    }

    public static void update(ServerLevel level, BlockPos pos, DyeFilter filter) {
        if (filter == null) FilterAttachment.remove(level, pos);
        else FilterAttachment.put(level, pos, filter);

        Display.ItemDisplay display = findDisplay(level, pos);

        if (filter != null) {
            if (display == null) {
                display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
                display.setPos(Vec3.atCenterOf(pos));
                display.addTag(DISPLAY_TAG);
                level.addFreshEntity(display);
            }

            ItemStack shown = filter.whitelist()
                    ? Utils.DYE_TO_CONCRETE.get(filter.color())
                    : Utils.DYE_TO_STAINED_GLASS.get(filter.color());
            display.getSlot(0).set(shown.copy());

            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.DETECTOR_RAIL)) {
                RailShape shape = state.getValue(DetectorRailBlock.SHAPE);
                ((DisplayAccessor) display).minecart_routing$setTransformation(Utils.shapeTransformation(shape));
            }
        } else if (display != null) {
            display.discard();
        }
    }

    public static Display.ItemDisplay findDisplay(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        AABB box = new AABB(center.add(-1, -1, -1), center.add(1, 1, 1));
        List<Display.ItemDisplay> existing = level.getEntities(
                EntityType.ITEM_DISPLAY,
                box,
                e -> e.entityTags().contains(DISPLAY_TAG)
        );
        return existing.isEmpty() ? null : existing.get(0);
    }
}
