package ec.brooke.minecartrouting.feature;

import ec.brooke.minecartrouting.store.DyeFilter;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class Router {

    public static boolean anyMatches(List<? extends AbstractMinecart> carts, DyeFilter filter) {
        for (AbstractMinecart cart : carts) {
            if (testEntity(cart, filter)) return true;
        }
        return false;
    }

    private static boolean testEntity(Entity entity, DyeFilter filter) {
        if (checkInventory(entity, filter)) return true;
        for (Entity passenger : entity.getPassengers()) {
            if (testEntity(passenger, filter)) return true;
        }
        return false;
    }

    private static boolean checkInventory(Entity entity, DyeFilter filter) {
        if (entity instanceof Player player) {
            return scanContainer(player.getInventory(), filter);
        }
        if (entity instanceof ContainerEntity vehicle) {
            NonNullList<ItemStack> stacks = vehicle.getItemStacks();
            return scanStacks(stacks, filter);
        }
        if (entity instanceof Container inv) {
            return scanContainer(inv, filter);
        }
        return false;
    }

    private static boolean scanContainer(Container inv, DyeFilter filter) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (matches(inv.getItem(i), filter)) return true;
        }
        return false;
    }

    private static boolean scanStacks(Iterable<ItemStack> stacks, DyeFilter filter) {
        for (ItemStack stack : stacks) {
            if (matches(stack, filter)) return true;
        }
        return false;
    }

    private static boolean matches(ItemStack stack, DyeFilter filter) {
        if (!stack.is(Items.FILLED_MAP)) return false;
        DyeColor color = Ticket.getTicket(stack);
        return color != null && filter.test(color);
    }
}
