package archives.tater.marksman;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

import static archives.tater.marksman.Util.getTargetPos;
import static archives.tater.marksman.Util.minBy;


public class Marksman implements ModInitializer {
	public static final String MOD_ID = "marksman";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final TagKey<Item> COIN_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "coin"));

	public static final float COIN_VEL_MULT = 2;
	public static final float COIN_VERT_VEL_ADD = 0.2f;
	public static final float OWN_VERT_VEL_MULT = 0.5f;

	public static final ProjectileDeflection AIM_AT_TARGET = (projectile, hitEntity, random) -> {
		if (hitEntity instanceof Ricoshottable ricoshottable)
			ricoshottable.marksman$setRicoshotted();
		
		var owner = projectile.getOwner() instanceof PlayerEntity player
				? player
				: hitEntity instanceof ItemEntity itemEntity && itemEntity.getOwner() != null
						? itemEntity.getOwner()
						: projectile.getOwner();
		var target = owner != null && hitEntity != null ? getTarget(projectile.getWorld(), owner, hitEntity, random) : null;

		if (hitEntity != null)
			projectile.setPosition(hitEntity.getPos());
        var targetVelocity = target == null ? Vec3d.ZERO : target.getMovement();
		var targetDelta = target == null
				? new Vec3d(2 * random.nextDouble() - 1, 2 * random.nextDouble() - 1, 2 * random.nextDouble() - 1)
				: getTargetPos(target, 1 / 3.0).subtract(projectile.getPos());
		var power = (float) projectile.getVelocity().length() + 0.5f;
		var approxTime = MathHelper.ceil(targetDelta.length() / power);
		var fallen = target == null || target.isOnGround() ? 0 : 0.5 * -target.getFinalGravity() * approxTime * approxTime;
		var aimDelta = targetDelta.add(targetVelocity.multiply(approxTime)).add(0, fallen, 0);
		var horizontalDistance = aimDelta.multiply(1, 0, 1).length();

		if (projectile instanceof PersistentProjectileEntity persistentProjectile)
			persistentProjectile.setDamage(persistentProjectile.getDamage() + 1f);
		projectile.setOwner(owner);
		projectile.setVelocity(aimDelta.x, aimDelta.y + (projectile.hasNoGravity() || projectile instanceof ExplosiveProjectileEntity ? 0 : horizontalDistance * 0.2F * 1.6f / power), targetDelta.z, power, 0);
		projectile.setPosition(projectile.getPos().subtract(projectile.getVelocity()));
		projectile.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.8f, 0.8f + 0.2f * random.nextFloat());
	};

	public static @Nullable Entity getTarget(World world, Entity owner, Entity hitEntity, Random random) {
		var ricoshottables = world.getOtherEntities(hitEntity, Box.of(hitEntity.getPos(), 16, 16, 16), Ricoshottable::canBeRicoshotted);
		if (!ricoshottables.isEmpty())
			return minBy(ricoshottables, entity -> entity.squaredDistanceTo(hitEntity));
		if (!(owner instanceof LivingEntity livingEntity)) return null;
		if (livingEntity instanceof MobEntity mobEntity && isValidTarget(hitEntity, mobEntity.getTarget()))
			return mobEntity.getTarget();
		if (isValidTarget(hitEntity, livingEntity.getAttacking())) return livingEntity.getAttacking();
		if (isValidTarget(hitEntity, livingEntity.getLastAttacker())) return livingEntity.getLastAttacker();
		var targets = world.getOtherEntities(hitEntity, Box.of(hitEntity.getPos(), 64, 16, 64), Util.and(getTargetPredicate(owner), entity1 -> isValidTarget(hitEntity, entity1)));
		if (!targets.isEmpty())
			return minBy(targets, entity -> entity.squaredDistanceTo(hitEntity));
		return null;
	}

	private static boolean isValidTarget(Entity source, @Nullable Entity target) {
		if (target == null || !target.isAlive()) return false;
		var targetPos = getTargetPos(target, 0.75);
        return targetPos.isWithinRangeOf(source.getPos(), 64, 16)
				&& source.getWorld().raycast(new RaycastContext(source.getPos(), targetPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, source)).getType() != HitResult.Type.BLOCK;
    }

	private static @Nullable Predicate<Entity> getTargetPredicate(Entity entity) {
		return entity instanceof MobEntity mobEntity
				? entity1 -> entity1 instanceof LivingEntity livingEntity && mobEntity.canTarget(livingEntity)
				: entity instanceof PlayerEntity
					? entity1 -> entity1 instanceof Monster || entity1 instanceof MobEntity mobEntity && mobEntity.getTarget() == entity || entity1 instanceof LivingEntity livingEntity && livingEntity.getAttacking() == entity
					: null;
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
	}
}
