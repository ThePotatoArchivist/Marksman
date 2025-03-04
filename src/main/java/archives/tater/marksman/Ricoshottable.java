package archives.tater.marksman;

import net.minecraft.entity.Entity;

public interface Ricoshottable {
    boolean marksman$canBeRicoshotted();

    void marksman$setRicoshotted();

    static boolean canBeRicoshotted(Entity entity) {
        return entity instanceof Ricoshottable ricoshottable && ricoshottable.marksman$canBeRicoshotted();
    }
}
