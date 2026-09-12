package exloran.bright.mixin;

import exloran.bright.Bright;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * HitboxMixin — Bright.config.hitboxActive acikken hedef varliklarin
 * gercek carpisma (hitbox) boyutlarini buyutur.
 */
@Mixin(Entity.class)
public class HitboxMixin {

    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void bright_expandDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (Bright.config == null || !Bright.config.hitboxActive) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        Entity self = (Entity) (Object) this;
        if (mc.player != null && self == mc.player) return;

        EntityDimensions orig = cir.getReturnValue();
        cir.setReturnValue(EntityDimensions.changing(
                orig.width() * Bright.config.xzExpand,
                orig.height() * Bright.config.yExpand
        ));
    }
}
