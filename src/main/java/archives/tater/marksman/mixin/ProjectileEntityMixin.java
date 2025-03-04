package archives.tater.marksman.mixin;

import archives.tater.marksman.Ricoshottable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileEntity.class)
public class ProjectileEntityMixin {
    @WrapOperation(
            method = "canHit(Lnet/minecraft/entity/Entity;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;canBeHitByProjectile()Z")
    )
    private boolean checkCoin(Entity instance, Operation<Boolean> original) {
        return original.call(instance) || Ricoshottable.canBeRicoshotted(instance);
    }
}
