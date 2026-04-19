package ec.brooke.minecartrouting.feature;

import ec.brooke.minecartrouting.store.Filter;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class Router {

    /**
     * Returns true if any cart (or its recursive passengers) carries a ticket
     * whose tag is in the filter's tag list. Independent of whitelist mode --
     * this answers "is the ticket physically present" not "does the filter
     * match." The mixin uses this together with the whitelist flag to compute
     * the actual match outcome and to produce the right action-bar message.
     */
    public static boolean isAnyTagCarried(List<? extends AbstractMinecart> carts, Filter filter) {
        for (AbstractMinecart cart : carts) {
            if (entityCarriesAnyTag(cart, filter)) return true;
        }
        return false;
    }

    public static void notifyPassengers(List<? extends AbstractMinecart> carts, Filter filter, boolean carried) {
        String label = filter.shortText();
        boolean matched = filter.whitelist() == carried;

        Component msg;
        if (matched && carried) {
            msg = Component.literal(label + " ticket matched, rerouting");
        } else if (matched) {
            msg = Component.literal("No " + label + " ticket, rerouting");
        } else if (carried) {
            msg = Component.literal(label + " ticket blocked, staying on path");
        } else {
            msg = Component.literal(label + " ticket not found, staying on path");
        }

        for (AbstractMinecart cart : carts) {
            for (Player p : collectPlayers(cart)) {
                p.sendOverlayMessage(msg);
            }
        }
    }

    private static List<Player> collectPlayers(Entity entity) {
        List<Player> players = new ArrayList<>();
        collectPlayersRecursive(entity, players);
        return players;
    }

    private static void collectPlayersRecursive(Entity entity, List<Player> out) {
        if (entity instanceof Player p) out.add(p);
        for (Entity passenger : entity.getPassengers()) {
            collectPlayersRecursive(passenger, out);
        }
    }

    private static boolean entityCarriesAnyTag(Entity entity, Filter filter) {
        if (containerHasTag(entity, filter)) return true;
        for (Entity passenger : entity.getPassengers()) {
            if (entityCarriesAnyTag(passenger, filter)) return true;
        }
        return false;
    }

    private static boolean containerHasTag(Entity entity, Filter filter) {
        if (entity instanceof Player player) {
            return scanContainerForTag(player.getInventory(), filter);
        }
        if (entity instanceof ContainerEntity vehicle) {
            NonNullList<ItemStack> stacks = vehicle.getItemStacks();
            return scanStacksForTag(stacks, filter);
        }
        if (entity instanceof Container inv) {
            return scanContainerForTag(inv, filter);
        }
        return false;
    }

    private static boolean scanContainerForTag(Container inv, Filter filter) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (stackTagInList(inv.getItem(i), filter)) return true;
        }
        return false;
    }

    private static boolean scanStacksForTag(Iterable<ItemStack> stacks, Filter filter) {
        for (ItemStack stack : stacks) {
            if (stackTagInList(stack, filter)) return true;
        }
        return false;
    }

    private static boolean stackTagInList(ItemStack stack, Filter filter) {
        if (!stack.is(Items.FILLED_MAP)) return false;
        String tag = Ticket.getTicket(stack);
        return tag != null && filter.tags().contains(tag);
    }
}
