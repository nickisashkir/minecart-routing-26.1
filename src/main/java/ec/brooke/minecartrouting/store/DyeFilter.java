package ec.brooke.minecartrouting.store;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;

public record DyeFilter(DyeColor color, boolean whitelist) {

    public static final Codec<DyeFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DyeColor.CODEC.fieldOf("color").forGetter(DyeFilter::color),
            Codec.BOOL.fieldOf("whitelist").forGetter(DyeFilter::whitelist)
    ).apply(instance, DyeFilter::new));

    public DyeFilter invert() {
        return new DyeFilter(color, !whitelist);
    }

    public boolean test(DyeColor other) {
        return whitelist == (this.color == other);
    }
}
