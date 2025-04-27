package archives.tater.marksman;

import net.minecraft.entity.Entity;

public interface Ricoshottable {
    boolean marksman$canBeRicoshotted(boolean requireAge);

    default boolean marksman$canBeRicoshotted() {
        return marksman$canBeRicoshotted(true);
    }

    void marksman$setRicoshotted();

    static boolean canBeRicoshotted(Entity entity) {
        return canBeRicoshotted(entity, true);
    }

    static boolean canBeRicoshotted(Entity entity, boolean requireAge) {
        return entity instanceof Ricoshottable ricoshottable && ricoshottable.marksman$canBeRicoshotted(requireAge);
    }
}
