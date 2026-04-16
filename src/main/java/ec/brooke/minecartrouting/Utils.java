package ec.brooke.minecartrouting;

import com.mojang.math.Transformation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;

public class Utils {

    public static final Map<Item, DyeColor> ITEM_TO_DYE = Map.ofEntries(
            Map.entry(Items.WHITE_DYE, DyeColor.WHITE),
            Map.entry(Items.LIGHT_GRAY_DYE, DyeColor.LIGHT_GRAY),
            Map.entry(Items.GRAY_DYE, DyeColor.GRAY),
            Map.entry(Items.BLACK_DYE, DyeColor.BLACK),
            Map.entry(Items.BROWN_DYE, DyeColor.BROWN),
            Map.entry(Items.RED_DYE, DyeColor.RED),
            Map.entry(Items.ORANGE_DYE, DyeColor.ORANGE),
            Map.entry(Items.YELLOW_DYE, DyeColor.YELLOW),
            Map.entry(Items.LIME_DYE, DyeColor.LIME),
            Map.entry(Items.GREEN_DYE, DyeColor.GREEN),
            Map.entry(Items.CYAN_DYE, DyeColor.CYAN),
            Map.entry(Items.LIGHT_BLUE_DYE, DyeColor.LIGHT_BLUE),
            Map.entry(Items.BLUE_DYE, DyeColor.BLUE),
            Map.entry(Items.PURPLE_DYE, DyeColor.PURPLE),
            Map.entry(Items.MAGENTA_DYE, DyeColor.MAGENTA),
            Map.entry(Items.PINK_DYE, DyeColor.PINK)
    );

    public static final Map<DyeColor, ItemStack> DYE_TO_CONCRETE = Map.ofEntries(
            Map.entry(DyeColor.WHITE, new ItemStack(Items.WHITE_CONCRETE)),
            Map.entry(DyeColor.LIGHT_GRAY, new ItemStack(Items.LIGHT_GRAY_CONCRETE)),
            Map.entry(DyeColor.GRAY, new ItemStack(Items.GRAY_CONCRETE)),
            Map.entry(DyeColor.BLACK, new ItemStack(Items.BLACK_CONCRETE)),
            Map.entry(DyeColor.BROWN, new ItemStack(Items.BROWN_CONCRETE)),
            Map.entry(DyeColor.RED, new ItemStack(Items.RED_CONCRETE)),
            Map.entry(DyeColor.ORANGE, new ItemStack(Items.ORANGE_CONCRETE)),
            Map.entry(DyeColor.YELLOW, new ItemStack(Items.YELLOW_CONCRETE)),
            Map.entry(DyeColor.LIME, new ItemStack(Items.LIME_CONCRETE)),
            Map.entry(DyeColor.GREEN, new ItemStack(Items.GREEN_CONCRETE)),
            Map.entry(DyeColor.CYAN, new ItemStack(Items.CYAN_CONCRETE)),
            Map.entry(DyeColor.LIGHT_BLUE, new ItemStack(Items.LIGHT_BLUE_CONCRETE)),
            Map.entry(DyeColor.BLUE, new ItemStack(Items.BLUE_CONCRETE)),
            Map.entry(DyeColor.PURPLE, new ItemStack(Items.PURPLE_CONCRETE)),
            Map.entry(DyeColor.MAGENTA, new ItemStack(Items.MAGENTA_CONCRETE)),
            Map.entry(DyeColor.PINK, new ItemStack(Items.PINK_CONCRETE))
    );

    public static final Map<DyeColor, ItemStack> DYE_TO_STAINED_GLASS = Map.ofEntries(
            Map.entry(DyeColor.WHITE, new ItemStack(Items.WHITE_STAINED_GLASS)),
            Map.entry(DyeColor.LIGHT_GRAY, new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS)),
            Map.entry(DyeColor.GRAY, new ItemStack(Items.GRAY_STAINED_GLASS)),
            Map.entry(DyeColor.BLACK, new ItemStack(Items.BLACK_STAINED_GLASS)),
            Map.entry(DyeColor.BROWN, new ItemStack(Items.BROWN_STAINED_GLASS)),
            Map.entry(DyeColor.RED, new ItemStack(Items.RED_STAINED_GLASS)),
            Map.entry(DyeColor.ORANGE, new ItemStack(Items.ORANGE_STAINED_GLASS)),
            Map.entry(DyeColor.YELLOW, new ItemStack(Items.YELLOW_STAINED_GLASS)),
            Map.entry(DyeColor.LIME, new ItemStack(Items.LIME_STAINED_GLASS)),
            Map.entry(DyeColor.GREEN, new ItemStack(Items.GREEN_STAINED_GLASS)),
            Map.entry(DyeColor.CYAN, new ItemStack(Items.CYAN_STAINED_GLASS)),
            Map.entry(DyeColor.LIGHT_BLUE, new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS)),
            Map.entry(DyeColor.BLUE, new ItemStack(Items.BLUE_STAINED_GLASS)),
            Map.entry(DyeColor.PURPLE, new ItemStack(Items.PURPLE_STAINED_GLASS)),
            Map.entry(DyeColor.MAGENTA, new ItemStack(Items.MAGENTA_STAINED_GLASS)),
            Map.entry(DyeColor.PINK, new ItemStack(Items.PINK_STAINED_GLASS))
    );

    private static final float PI2 = (float) Math.PI / 2;
    private static final float PI4 = (float) Math.PI / 4;
    private static final Map<RailShape, Integer> SHAPES = Map.of(
            RailShape.ASCENDING_NORTH, 0,
            RailShape.ASCENDING_WEST, 1,
            RailShape.ASCENDING_SOUTH, 2,
            RailShape.ASCENDING_EAST, 3
    );

    public static Transformation shapeTransformation(RailShape shape) {
        if (shape.name().contains("ASCENDING")) {
            return new Transformation(
                    new Vector3f(0.5f, 0.5625f, 0.5f),
                    new Quaternionf().rotateY(PI2 * SHAPES.get(shape)).rotateX(PI4),
                    new Vector3f(0.25f, 0.05f, 0.25f * 1.41f),
                    new Quaternionf()
            );
        }
        return new Transformation(
                new Vector3f(0.5f, 0.075f, 0.5f),
                new Quaternionf(),
                new Vector3f(0.25f, 0.05f, 0.25f),
                new Quaternionf()
        );
    }
}
