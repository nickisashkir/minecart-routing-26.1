package ec.brooke.minecartrouting.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import ec.brooke.minecartrouting.feature.Assigner;
import ec.brooke.minecartrouting.feature.Router;
import ec.brooke.minecartrouting.store.Filter;
import ec.brooke.minecartrouting.store.FilterAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DetectorRailBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * Intercepts the cart list that DetectorRailBlock uses to decide whether the
 * rail should be powered. If the rail has a stored filter and no cart on the
 * rail carries a matching ticket, we return an empty list so vanilla decides
 * the rail is empty and leaves it unpowered.
 *
 * <p>Verified against the 26.1 deobfuscated jar: the target is
 * {@code DetectorRailBlock#checkPressed(Level, BlockPos, BlockState)} and the
 * inner call we wrap is {@code DetectorRailBlock#getInteractingMinecartOfType}
 * (a private method on DetectorRailBlock itself, not BaseRailBlock).
 */
@Mixin(DetectorRailBlock.class)
public abstract class DetectorRailBlockMixin {

    @ModifyExpressionValue(
            method = "checkPressed(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/DetectorRailBlock;getInteractingMinecartOfType(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/lang/Class;Ljava/util/function/Predicate;)Ljava/util/List;"
            )
    )
    private List<AbstractMinecart> minecart_routing$filterByTicket(
            List<AbstractMinecart> original,
            @Local(argsOnly = true) Level level,
            @Local(argsOnly = true) BlockPos pos
    ) {
        Filter filter = FilterAttachment.get(level, pos);
        if (filter == null) return original;

        if (!level.getBlockState(pos).is(Blocks.DETECTOR_RAIL)) {
            if (level instanceof ServerLevel serverLevel) {
                Assigner.updateFilter(serverLevel, pos, null);
            }
            return original;
        }

        boolean carried = Router.isAnyTagCarried(original, filter);
        boolean matched = filter.whitelist() == carried;
        Router.notifyPassengers(original, filter, carried);
        return matched ? original : List.of();
    }
}
