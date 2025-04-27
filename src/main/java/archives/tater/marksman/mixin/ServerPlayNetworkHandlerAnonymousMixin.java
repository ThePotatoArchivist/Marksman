package archives.tater.marksman.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.network.ServerPlayNetworkHandler$1")
public class ServerPlayNetworkHandlerAnonymousMixin {
    @Shadow @Final Entity field_28962;
    @Shadow @Final ServerPlayNetworkHandler field_28963;

    @Inject(
            method = "attack",
            at = @At("HEAD"),
            cancellable = true)
    private void allowRicoshottableItems(CallbackInfo ci) {
        // This is kind of hacky but it's really the only way to do it without getting mixinextras expressions
        if (!(field_28962 instanceof ItemEntity)) return;
        field_28963.player.attack(field_28962);
        ci.cancel();
    }
}
