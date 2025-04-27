package archives.tater.marksman.mixin;

import archives.tater.marksman.Ricoshottable;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin {
    @SuppressWarnings("ConstantValue")
    @ModifyReturnValue(
            method = "canHit",
            at = @At("RETURN")
    )
    private boolean checkCoinItem(boolean original) {
        return original || Ricoshottable.canBeRicoshotted((Entity) (Object) this, false);
    }

    @ModifyReturnValue(
            method = "getTargetingMargin",
            at = @At("RETURN")
    )
    private float increaseTargetMargin(float original) {
        return Ricoshottable.canBeRicoshotted((Entity) (Object) this, false) ? 1f : original;
    }
}
