package archives.tater.marksman.mixin;

import archives.tater.marksman.CoinProjectileEntity;
import archives.tater.marksman.Marksman;
import archives.tater.marksman.Ricoshottable;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Unit;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements Ricoshottable {
	@Shadow public abstract ItemStack getStack();

	@Shadow public abstract int getItemAge();

	public ItemEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Override
	public ProjectileDeflection getProjectileDeflection(ProjectileEntity projectile) {
		return marksman$canBeRicoshotted() ? Marksman.AIM_AT_TARGET : super.getProjectileDeflection(projectile);
	}

	@Inject(
			method = "damage",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;scheduleVelocityUpdate()V"),
			cancellable = true)
	private void handleCoinPunch(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!source.isDirect() || !marksman$canBeRicoshotted(false)) return;
		var attacker = source.getAttacker();
		if (attacker == null) return;
        var coinProjectile = new CoinProjectileEntity(getWorld(), attacker instanceof LivingEntity livingEntity ? livingEntity : null, getPos());
		coinProjectile.setVelocity(attacker, attacker.getPitch(), attacker.getYaw(), 0f, 1.5f, 0.1f);
		getWorld().spawnEntity(coinProjectile);
		discard();
		cir.setReturnValue(true);
	}

	@ModifyReturnValue(
			method = "isAttackable",
			at = @At("RETURN")
	)
	private boolean makeAttackable(boolean original) {
		return original || marksman$canBeRicoshotted(false);
	}

	@Override
	public boolean marksman$canBeRicoshotted(boolean requireAge) {
		return !hasAttached(Marksman.RICOSHOTTED) && (!requireAge || getItemAge() > 6) && !isOnGround() && getStack().isIn(Marksman.COIN_TAG);
	}

	@Override
	public void marksman$setRicoshotted() {
		setAttached(Marksman.RICOSHOTTED, Unit.INSTANCE);
	}
}
