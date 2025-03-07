package archives.tater.marksman.mixin;

import archives.tater.marksman.Marksman;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);

    @Shadow public abstract SoundCategory getSoundCategory();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(
            method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/random/Random;nextFloat()F", ordinal = 3)
    )
    private float removeRandomness1(float original, @Local(argsOnly = true) ItemStack stack) {
        return stack.isIn(Marksman.COIN_TAG) ? 0 : original;
    }

    @ModifyExpressionValue(
            method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
            at = @At(value = "CONSTANT", args = "floatValue=0.1", ordinal = 1)
    )
    private float removeRandomness2(float original, @Local(argsOnly = true) ItemStack stack) {
        return stack.isIn(Marksman.COIN_TAG) ? 0 : original;
    }

    @WrapOperation(
            method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;setVelocity(DDD)V", ordinal = 1)
    )
    private void addOwnVelocity(ItemEntity instance, double vx, double vy, double vz, Operation<Void> original, @Local(argsOnly = true) ItemStack stack) {
        if (!stack.isIn(Marksman.COIN_TAG)) {
            original.call(instance, vx, vy, vz);
            return;
        }
        original.call(instance,
                (getX() - prevX) + Marksman.COIN_VEL_MULT * vx,
                Marksman.OWN_VERT_VEL_MULT * (getY() - prevY) + Marksman.COIN_VEL_MULT * vy + Marksman.COIN_VERT_VEL_ADD,
                (getZ() -  prevZ) + Marksman.COIN_VEL_MULT * vz);
    }

    @Inject(
            method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
            at = @At("HEAD")
    )
    private void playSound(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        if (!getWorld().isClient && !throwRandomly && stack.isIn(Marksman.COIN_TAG))
            getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_ARROW_HIT_PLAYER, getSoundCategory(), 0.2f, 1.2f + 0.2f * random.nextFloat());
    }
}
