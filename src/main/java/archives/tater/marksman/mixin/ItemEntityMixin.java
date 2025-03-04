package archives.tater.marksman.mixin;

import archives.tater.marksman.Marksman;
import archives.tater.marksman.Ricoshottable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements Ricoshottable {
	@Shadow public abstract ItemStack getStack();

	@Shadow public abstract int getItemAge();

	@Unique
	private boolean marksman$ricoshotted = false;

	public ItemEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Override
	public ProjectileDeflection getProjectileDeflection(ProjectileEntity projectile) {
		return marksman$canBeRicoshotted() ? Marksman.AIM_AT_TARGET : super.getProjectileDeflection(projectile);
	}

	@Override
	public boolean marksman$canBeRicoshotted() {
		return !marksman$ricoshotted && getItemAge() > 6 && !isOnGround() && getStack().isIn(Marksman.COIN_TAG);
	}

	@Override
	public void marksman$setRicoshotted() {
		marksman$ricoshotted = true;
	}
}
