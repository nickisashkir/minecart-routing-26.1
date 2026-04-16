package ec.brooke.minecartrouting.feature;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public class Ticket {

    public static final String TICKET_KEY = "minecart_routing:ticket";

    public static DyeColor getTicket(ItemStack stack) {
        if (!stack.is(Items.FILLED_MAP)) return null;

        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom == null) return null;

        CompoundTag nbt = custom.copyTag();
        Optional<String> name = nbt.getString(TICKET_KEY);
        if (name.isEmpty()) return null;

        try {
            return DyeColor.valueOf(name.get().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean isTicket(ItemStack stack) {
        return getTicket(stack) != null;
    }
}
