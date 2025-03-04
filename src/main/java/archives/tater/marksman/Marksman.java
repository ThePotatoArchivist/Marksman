package archives.tater.marksman;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Predicate;

public class Marksman implements ModInitializer {
	public static final String MOD_ID = "marksman";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final TagKey<Item> COIN_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "coin"));

	public static final ProjectileDeflection AIM_AT_TARGET = (projectile, hitEntity, random) -> {
		if (hitEntity instanceof Ricoshottable ricoshottable)
			ricoshottable.marksman$setRicoshotted();

		var owner = Objects.requireNonNullElse(hitEntity instanceof ItemEntity itemEntity ? itemEntity.getOwner() : null, projectile.getOwner());
		var target = getTarget(projectile.getWorld(), owner, hitEntity, random);

		if (hitEntity != null)
			projectile.setPosition(hitEntity.getPos());
		var targetPos = target == null ? projectile.getPos().add(2 * random.nextDouble() - 1, 2 * random.nextDouble() - 1, 2 * random.nextDouble() - 1) : getTargetPos(target, 1 / 3.0);
		var difference = targetPos.subtract(projectile.getPos());
        var horizontalDistance = difference.multiply(1, 0, 1).length();
		if (projectile instanceof PersistentProjectileEntity persistentProjectile)
			persistentProjectile.setDamage(persistentProjectile.getDamage() + 1f);
		var power = (float) projectile.getVelocity().length() + 0.5f;
		projectile.setVelocity(difference.x, difference.y + horizontalDistance * 0.2F * 1.6f / power, difference.z, power, 0);
		projectile.setPosition(projectile.getPos().subtract(projectile.getVelocity()));
		projectile.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
	};

	public static @Nullable Entity getTarget(World world, Entity owner, Entity hitEntity, Random random) {
		var ricoshottables = world.getOtherEntities(hitEntity, Box.of(hitEntity.getPos(), 8, 8, 8), Ricoshottable::canBeRicoshotted);
		if (!ricoshottables.isEmpty())
			return ricoshottables.getFirst();
		if (!(owner instanceof LivingEntity livingEntity)) return null;
		if (livingEntity instanceof MobEntity mobEntity && hasLineOfSight(owner, mobEntity.getTarget()))
			return mobEntity.getTarget();
		if (hasLineOfSight(owner, livingEntity.getAttacking())) return livingEntity.getAttacking();
		if (hasLineOfSight(owner, livingEntity.getLastAttacker())) return livingEntity.getLastAttacker();
		var targets = world.getOtherEntities(owner, Box.of(hitEntity.getPos(), 32, 32, 32), and(getTargetPredicate(owner), entity1 -> hasLineOfSight(hitEntity, entity1)));
		if (!targets.isEmpty())
			return targets.get(random.nextInt(targets.size()));
		return null;
	}

	private static Vec3d getTargetPos(Entity entity, double heightScale) {
		return new Vec3d(entity.getX(), entity.getBodyY(heightScale), entity.getZ());
	}

	private static boolean hasLineOfSight(Entity source, @Nullable Entity target) {
		if (target == null) return false;
		return source.getWorld().raycast(new RaycastContext(source.getPos(), getTargetPos(target, 0.75), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, source)).getType() != HitResult.Type.BLOCK;
	}

	private static @Nullable Predicate<Entity> getTargetPredicate(Entity entity) {
		return entity instanceof MobEntity mobEntity
				? entity1 -> entity1 instanceof LivingEntity livingEntity && mobEntity.canTarget(livingEntity)
				: entity instanceof PlayerEntity
					? entity1 -> entity1 instanceof Monster
					: null;
	}

	public static <T> @NotNull Predicate<T> and(@Nullable Predicate<T> a, @Nullable Predicate<T> b) {
        return a == null
				? b == null ? (o -> true) : b
				: b == null ? a : a.and(b);
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
	}
}
