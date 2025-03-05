package archives.tater.marksman;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

public class Util {
    public static <T, U extends Comparable<U>> @Nullable T minBy(Iterable<T> items, Function<T, U> transform) {
        T lowestItem = null;
        U lowestValue = null;
        for (var item : items) {
            var value = transform.apply(item);
            if (lowestValue == null || value.compareTo(lowestValue) < 0) {
                lowestValue = value;
                lowestItem = item;
            }
        }
        return lowestItem;
    }

    static Vec3d getTargetPos(Entity entity, double heightScale) {
        return new Vec3d(entity.getX(), entity.getBodyY(heightScale), entity.getZ());
    }

    public static <T> @NotNull Predicate<T> and(@Nullable Predicate<T> a, @Nullable Predicate<T> b) {
return a == null
                ? b == null ? (o -> true) : b
                : b == null ? a : a.and(b);
    }
}
