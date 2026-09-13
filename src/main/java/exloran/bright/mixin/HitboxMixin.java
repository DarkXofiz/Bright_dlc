package exloran.bright.mixin;

import exloran.bright.Bright;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * HitboxMixin — Bright.config.hitboxActive acikken hedef varliklarin
 * carpisma kutusunu (bounding box) her cagrildiginda buyutur.
 * getDimensions() yerine dogrudan getBoundingBox() hedeflenir; boylece
 * onbellege alinmis (cached) boyut nedeniyle degisikligin gec
 * yansimasi sorunu yasanmaz, etki aninda uygulanir.
 */
@Mixin(Entity.class)
public class HitboxMixin {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void bright_expandBoundingBox(CallbackInfoReturnable<Box> cir) {
        if (Bright.config == null || !Bright.config.hitboxActive) return;

        Entity self = (Entity) (Object) this;
        if (!(self instanceof LivingEntity)) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && self == mc.player) return;

        Box orig = cir.getReturnValue();
        double cx = (orig.minX + orig.maxX) / 2.0;
        double cz = (orig.minZ + orig.maxZ) / 2.0;
        double halfW = (orig.maxX - orig.minX) / 2.0 * Bright.config.xzExpand;
        double halfH = (orig.maxY - orig.minY) * Bright.config.yExpand;

        cir.setReturnValue(new Box(
                cx - halfW, orig.minY, cz - halfW,
                cx + halfW, orig.minY + halfH, cz + halfW
        ));
    }
}
