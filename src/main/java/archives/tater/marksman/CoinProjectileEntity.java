package archives.tater.marksman;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CoinProjectileEntity extends ThrownItemEntity {
    public CoinProjectileEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public CoinProjectileEntity(ItemStack stack, double x, double y, double z, World world) {
        super(Marksman.COIN_PROJECTILE, x, y, z, world);
        setItem(stack);
    }

    public CoinProjectileEntity(ItemStack stack, LivingEntity owner, World world) {
        super(Marksman.COIN_PROJECTILE, owner, world);
        setItem(stack);
    }

    public CoinProjectileEntity(World world, @Nullable LivingEntity owner, ItemStack stack, Vec3d pos) {
        this(stack, pos.x, pos.y, pos.z, world);
        setOwner(owner);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.GOLD_NUGGET;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        entity.damage(this.getDamageSources().thrown(this, this.getOwner()), 1f); // TODO custom damage source
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            this.discard();
            getWorld().spawnEntity(new ItemEntity(getWorld(), getX(), getY(), getZ(), getStack(), 0, hitResult instanceof EntityHitResult ? 0.5 : 0, 0));
        }
    }
}
