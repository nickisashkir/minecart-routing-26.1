package ec.brooke.minecartrouting.store;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;

public record Filter(List<String> tags, boolean whitelist) {

    public static final Codec<Filter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("tags").forGetter(Filter::tags),
            Codec.BOOL.fieldOf("whitelist").forGetter(Filter::whitelist)
    ).apply(instance, Filter::new));

    public Filter invert() {
        return new Filter(tags, !whitelist);
    }

    public Filter withTag(String tag) {
        if (tags.contains(tag)) return this;
        List<String> newTags = new ArrayList<>(tags);
        newTags.add(tag);
        return new Filter(newTags, whitelist);
    }

    public boolean test(String ticketTag) {
        boolean found = tags.contains(ticketTag);
        return whitelist == found;
    }

    public DyeColor singleDyeColor() {
        if (tags.size() != 1) return null;
        try {
            return DyeColor.valueOf(tags.get(0).toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String displayText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(formatTag(tags.get(i)));
        }
        sb.append(whitelist ? " (whitelist)" : " (blacklist)");
        return sb.toString();
    }

    public String shortText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) sb.append(" | ");
            sb.append(formatTag(tags.get(i)));
        }
        return sb.toString();
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
}
