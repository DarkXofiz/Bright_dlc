package exloran.bright;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * BrightESPRenderer — ESP acikken canli varliklarin etrafina tel kafes kutu cizer.
 * "Duvar Icinden Gor" ayari kapaliysa derinlik testi normal kalir (sadece gorus
 * hattinda gorunur); acikken engelin arkasindan da gorunur.
 */
public class BrightESPRenderer {

    public static void register() {
        WorldRenderEvents.LAST.register(BrightESPRenderer::render);
    }

    private static void render(WorldRenderContext ctx) {
        if (Bright.config == null || !Bright.config.espActive) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        MatrixStack ms = ctx.matrixStack();
        if (ms == null) return;
        Vec3d cam = ctx.camera().getPos();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || le == mc.player || !le.isAlive()) continue;
            drawBox(ms, le.getBoundingBox(), cam,
                    Bright.config.espR, Bright.config.espG, Bright.config.espB, 0.9f,
                    Bright.config.espLineWidth, Bright.config.espThroughWalls);
        }
    }

    private static void drawBox(MatrixStack ms, Box box, Vec3d cam,
                                 float r, float g, float b, float a,
                                 float lineWidth, boolean throughWalls) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (throughWalls) RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(lineWidth);

        ms.push();
        ms.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f mat = ms.peek().getPositionMatrix();

        double x1 = box.minX, y1 = box.minY, z1 = box.minZ;
        double x2 = box.maxX, y2 = box.maxY, z2 = box.maxZ;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);

        // Alt kare
        line(buf, mat, x1, y1, z1, x2, y1, z1, r, g, b, a);
        line(buf, mat, x2, y1, z1, x2, y1, z2, r, g, b, a);
        line(buf, mat, x2, y1, z2, x1, y1, z2, r, g, b, a);
        line(buf, mat, x1, y1, z2, x1, y1, z1, r, g, b, a);
        // Ust kare
        line(buf, mat, x1, y2, z1, x2, y2, z1, r, g, b, a);
        line(buf, mat, x2, y2, z1, x2, y2, z2, r, g, b, a);
        line(buf, mat, x2, y2, z2, x1, y2, z2, r, g, b, a);
        line(buf, mat, x1, y2, z2, x1, y2, z1, r, g, b, a);
        // Dikey kenarlar
        line(buf, mat, x1, y1, z1, x1, y2, z1, r, g, b, a);
        line(buf, mat, x2, y1, z1, x2, y2, z1, r, g, b, a);
        line(buf, mat, x2, y1, z2, x2, y2, z2, r, g, b, a);
        line(buf, mat, x1, y1, z2, x1, y2, z2, r, g, b, a);

        tess.draw();
        ms.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void line(BufferBuilder buf, Matrix4f mat,
                              double x1, double y1, double z1,
                              double x2, double y2, double z2,
                              float r, float g, float b, float a) {
        buf.vertex(mat, (float) x1, (float) y1, (float) z1).color(r, g, b, a).next();
        buf.vertex(mat, (float) x2, (float) y2, (float) z2).color(r, g, b, a).next();
    }
}
