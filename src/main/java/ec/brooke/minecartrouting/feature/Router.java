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

    public static boolean anyMatches(List<? extends AbstractMinecart> carts, Filter filter) {
        for (AbstractMinecart cart : carts) {
            if (testEntity(cart, filter)) return true;
        }
        return false;
    }

    public static void notifyPassengers(List<? extends AbstractMinecart> carts, Filter filter, boolean matched) {
        String name = formatTag(filter.tag());
        Component msg = matched
                ? Component.literal(name + " ticket found, routing to " + name + " path")
                : Component.literal(name + " ticket not found, not staying on path");

        for (AbstractMinecart cart : carts) {
            collectPlayers(cart).forEach(p -> p.sendOverlayMessage(msg));
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

    static String formatTag(String tag) {
        String[] words = tag.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(Character.toUpperCase(words[i].charAt(0)));
            sb.append(words[i].substring(1));
        }
        return sb.toString();
    }

    private static boolean testEntity(Entity entity, Filter filter) {
        if (checkInventory(entity, filter)) return true;
        for (Entity passenger : entity.getPassengers()) {
            if (testEntity(passenger, filter)) return true;
        }
        return false;
    }

    private static boolean checkInventory(Entity entity, Filter filter) {
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

    private static boolean scanContainer(Container inv, Filter filter) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (matches(inv.getItem(i), filter)) return true;
        }
        return false;
    }

    private static boolean scanStacks(Iterable<ItemStack> stacks, Filter filter) {
        for (ItemStack stack : stacks) {
            if (matches(stack, filter)) return true;
        }
        return false;
    }

    private static boolean matches(ItemStack stack, Filter filter) {
        if (!stack.is(Items.FILLED_MAP)) return false;
        String tag = Ticket.getTicket(stack);
        return tag != null && filter.test(tag);
    }
}
