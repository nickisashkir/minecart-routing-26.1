package ec.brooke.minecartrouting.store;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;

public record Filter(String tag, boolean whitelist) {

    public static final Codec<Filter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("tag").forGetter(Filter::tag),
            Codec.BOOL.fieldOf("whitelist").forGetter(Filter::whitelist)
    ).apply(instance, Filter::new));

    public Filter invert() {
        return new Filter(tag, !whitelist);
    }

    public boolean test(String other) {
        return whitelist == (this.tag.equals(other));
    }

    public DyeColor dyeColor() {
        try {
            return DyeColor.valueOf(tag.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
