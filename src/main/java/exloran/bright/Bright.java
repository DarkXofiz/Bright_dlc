package exloran.bright;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

/**
 * Bright — Minecraft 1.20.1 Fabric istemci modu.
 * Sadece 3 modul: Hitbox, TriggerBot, ESP.
 * Ayar ekrani SADECE ModMenu uzerinden acilir; tus atamasi yok.
 */
public class Bright implements ClientModInitializer {

    public static BrightConfig config;

    private long lastAttack = 0L;

    @Override
    public void onInitializeClient() {
        config = BrightConfig.load();

        BrightESPRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            handleTrigger(client);
        });
    }

    // ── TriggerBot ───────────────────────────────────────────
    private void handleTrigger(MinecraftClient c) {
        if (!config.triggerActive) return;
        LivingEntity target = null;
        if (c.crosshairTarget instanceof EntityHitResult hr
                && hr.getEntity() instanceof LivingEntity le && le.isAlive())
            target = le;
        if (target == null) return;
        if (config.trigBlockShield && target.isBlocking()) return;
        if (config.trigBlockEating && isEating(target)) return;
        if (c.player.isBlocking()) return;
        if (c.player.getAttackCooldownProgress(0.5f) < 1.0f) return;

        long now = System.currentTimeMillis();
        if (now - lastAttack < config.triggerDelay) return;

        c.interactionManager.attackEntity(c.player, target);
        c.player.swingHand(Hand.MAIN_HAND);
        lastAttack = now;
    }

    private boolean isEating(LivingEntity e) {
        if (e == null || !e.isUsingItem()) return false;
        ItemStack u = e.getActiveItem();
        if (u.isEmpty()) return false;
        return u.getItem().isFood()
                || u.getItem() == Items.MILK_BUCKET || u.getItem() == Items.POTION
                || u.getItem() == Items.SPLASH_POTION || u.getItem() == Items.LINGERING_POTION;
    }

    // ══════════════════════════════════════════════════════════
    //  Yuvarlak kose ciziminde kullanilan yardimcilar (1.20.1 API)
    // ══════════════════════════════════════════════════════════
    public static void fillRound(MatrixStack ms, float x, float y, float w, float h, float r, int color) {
        float a = ((color >> 24) & 0xFF) / 255f, rv = ((color >> 16) & 0xFF) / 255f,
                gv = ((color >> 8) & 0xFF) / 255f, b = (color & 0xFF) / 255f;
        if (a <= 0) return;
        r = Math.min(r, Math.min(w, h) * 0.499f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f m4 = ms.peek().getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(m4, x + w / 2f, y + h / 2f, 0).color(rv, gv, b, a).next();
        float[] cx = {x + w - r, x + r, x + r, x + w - r}, cy = {y + r, y + r, y + h - r, y + h - r}, sa = {270f, 180f, 90f, 0f};
        for (int i = 0; i < 4; i++) for (int j = 0; j <= 12; j++) {
            double ang = Math.toRadians(sa[i] + j * 7.5);
            buf.vertex(m4, (float) (cx[i] + Math.cos(ang) * r), (float) (cy[i] + Math.sin(ang) * r), 0).color(rv, gv, b, a).next();
        }
        double ca = Math.toRadians(sa[0]);
        buf.vertex(m4, (float) (cx[0] + Math.cos(ca) * r), (float) (cy[0] + Math.sin(ca) * r), 0).color(rv, gv, b, a).next();
        tess.draw();
        RenderSystem.disableBlend();
    }

    public static void outlineRound(MatrixStack ms, float x, float y, float w, float h, float r, int color) {
        float a = ((color >> 24) & 0xFF) / 255f, rv = ((color >> 16) & 0xFF) / 255f,
                gv = ((color >> 8) & 0xFF) / 255f, b = (color & 0xFF) / 255f;
        if (a <= 0) return;
        r = Math.min(r, Math.min(w, h) * 0.499f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(1.3f);
        Matrix4f m4 = ms.peek().getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        float[] cx = {x + w - r, x + r, x + r, x + w - r}, cy = {y + r, y + r, y + h - r, y + h - r}, sa = {270f, 180f, 90f, 0f};
        for (int i = 0; i < 4; i++) for (int j = 0; j <= 12; j++) {
            double ang = Math.toRadians(sa[i] + j * 7.5);
            buf.vertex(m4, (float) (cx[i] + Math.cos(ang) * r), (float) (cy[i] + Math.sin(ang) * r), 0).color(rv, gv, b, a).next();
        }
        double ca = Math.toRadians(sa[0]);
        buf.vertex(m4, (float) (cx[0] + Math.cos(ca) * r), (float) (cy[0] + Math.sin(ca) * r), 0).color(rv, gv, b, a).next();
        tess.draw();
        RenderSystem.disableBlend();
    }

    public static int packRgb(float r, float g, float b, float a) {
        return ((int) (a * 255) & 0xFF) << 24 | ((int) (r * 255) & 0xFF) << 16 | ((int) (g * 255) & 0xFF) << 8 | ((int) (b * 255) & 0xFF);
    }

    // ══════════════════════════════════════════════════════════
    //  BRIGHT MENU — sadece ModMenu'den acilir (tus atamasi yok)
    // ══════════════════════════════════════════════════════════
    public static class BrightMenu extends Screen {

        private static final int MW = 210, MH = 112;
        private static final int SP_W = 220, SP_H = 150;
        private String settingsFor = null;

        public BrightMenu() { super(Text.literal("Bright")); }
        @Override public boolean shouldPause() { return false; }

        @Override
        public void render(DrawContext ctx, int mx, int my, float delta) {
            MatrixStack ms = ctx.getMatrices();
            int ox = ox(), oy = oy();

            ctx.fill(0, 0, width, height, 0x55000000);
            fillRound(ms, ox, oy, MW, MH, 8f, 0xF2101018);
            outlineRound(ms, ox, oy, MW, MH, 8f, 0xFF282840);
            fillRound(ms, ox, oy, MW, 24, 8f, 0xFF0A0A14);
            ctx.fill(ox, oy + 12, ox + MW, oy + 24, 0xFF0A0A14);
            ctx.drawCenteredTextWithShadow(textRenderer, "\u00a75\u25c8 \u00a7dBright \u00a75\u25c8", ox + MW / 2, oy + 7, 0xFFCC88FF);

            String[] mods = {"Hitbox", "Trigger", "ESP"};
            int row = oy + 32;
            for (String mod : mods) {
                boolean on = isOn(mod);
                boolean hov = hovI(mx, my, ox + 6, row, MW - 36, 22);
                boolean hovGear = hovI(mx, my, ox + MW - 32, row + 4, 26, 14);

                if (on) fillRound(ms, ox + 6, row, MW - 36, 22, 5f, 0xFF1E0040);
                else if (hov) fillRound(ms, ox + 6, row, MW - 36, 22, 5f, 0xFF161628);
                if (on) {
                    ctx.fill(ox + 6, row + 3, ox + 8, row + 19, 0xFFBB55FF);
                    ctx.fill(ox + 8, row + 3, ox + 10, row + 19, 0x44BB55FF);
                }
                ctx.drawTextWithShadow(textRenderer, mod, ox + 14, row + 7,
                        on ? 0xFFEEDDFF : (hov ? 0xFFAAA8CC : 0xFF505060));
                ctx.drawTextWithShadow(textRenderer, on ? "\u00a7a\u25cf" : "\u00a78\u25cf",
                        ox + MW - 44, row + 7, 0xFFFFFFFF);

                boolean gearActive = settingsFor != null && settingsFor.equals(mod);
                fillRound(ms, ox + MW - 32, row + 4, 26, 14, 4f,
                        gearActive ? 0xFF3A1A6A : (hovGear ? 0xFF201A38 : 0xFF0E0E1E));
                outlineRound(ms, ox + MW - 32, row + 4, 26, 14, 4f,
                        gearActive ? 0xFF8844CC : 0xFF333344);
                ctx.drawCenteredTextWithShadow(textRenderer, "\u00a77\u2699", ox + MW - 32 + 13, row + 4, 0xFF9988BB);

                row += 28;
            }

            ctx.drawTextWithShadow(textRenderer, "\u00a78LClick=ac/kapat  \u2699=ayarlar",
                    ox + 5, oy + MH - 10, 0xFF242434);

            if (settingsFor != null) renderSettingsPanel(ctx, ms, mx, my);

            super.render(ctx, mx, my, delta);
        }

        private void renderSettingsPanel(DrawContext ctx, MatrixStack ms, int mx, int my) {
            int ox = ox() + MW + 6, oy = oy();
            fillRound(ms, ox, oy, SP_W, SP_H, 8f, 0xF2101018);
            outlineRound(ms, ox, oy, SP_W, SP_H, 8f, 0xFF8844CC);
            fillRound(ms, ox, oy, SP_W, 22, 8f, 0xFF0A0A14);
            ctx.fill(ox, oy + 11, ox + SP_W, oy + 22, 0xFF0A0A14);
            ctx.drawCenteredTextWithShadow(textRenderer, "\u00a7d" + settingsFor + " \u00a78Ayarlari",
                    ox + SP_W / 2, oy + 6, 0xFFCC88FF);

            int py = oy + 28;
            switch (settingsFor) {
                case "Hitbox" -> {
                    py = slider(ctx, ms, ox + 8, py, SP_W - 16, "XZ Buyutme",
                            Math.round(config.xzExpand * 10), 5, 30, "x" + String.format("%.1f", config.xzExpand));
                    py = slider(ctx, ms, ox + 8, py, SP_W - 16, "Y Buyutme",
                            Math.round(config.yExpand * 10), 5, 30, "x" + String.format("%.1f", config.yExpand));
                }
                case "Trigger" -> {
                    py = slider(ctx, ms, ox + 8, py, SP_W - 16, "Gecikme", config.triggerDelay, 0, 300, "ms");
                    py = checkbox(ctx, ms, mx, my, ox + 8, py, "Kalkan Engelle", config.trigBlockShield);
                    py = checkbox(ctx, ms, mx, my, ox + 8, py, "Yemek Engelle", config.trigBlockEating);
                }
                case "ESP" -> {
                    int pc = packRgb(config.espR, config.espG, config.espB, 1f);
                    fillRound(ms, ox + 8, py, SP_W - 16, 16, 4f, pc);
                    outlineRound(ms, ox + 8, py, SP_W - 16, 16, 4f, 0xFF555566);
                    ctx.drawCenteredTextWithShadow(textRenderer, "Renk Onizleme", ox + SP_W / 2, py + 4, 0xFFFFFFFF);
                    py += 22;
                    py = slider(ctx, ms, ox + 8, py, SP_W - 16, "\u00a7cKirmizi", Math.round(config.espR * 255), 0, 255, "");
                    py = slider(ctx, ms, ox + 8, py, SP_W - 16, "\u00a7aYesil", Math.round(config.espG * 255), 0, 255, "");
                    py = slider(ctx, ms, ox + 8, py, SP_W - 16, "\u00a79Mavi", Math.round(config.espB * 255), 0, 255, "");
                    py = slider(ctx, ms, ox + 8, py, SP_W - 16, "Cizgi Kalinligi", Math.round(config.espLineWidth * 10), 5, 50,
                            String.format("%.1f", config.espLineWidth));
                    py = checkbox(ctx, ms, mx, my, ox + 8, py, "Duvar Icinden Gor", config.espThroughWalls);
                }
            }
        }

        private int slider(DrawContext ctx, MatrixStack ms, int x, int y, int w, String label, int val, int min, int max, String suf) {
            String disp = label + ": \u00a7f" + val + (suf.isEmpty() ? "" : "\u00a78 " + suf);
            ctx.drawTextWithShadow(textRenderer, disp, x, y, 0xFF9999BB);
            y += 11;
            fillRound(ms, x, y, w, 7, 3f, 0xFF0C0C20);
            float t = (float) (val - min) / (max - min);
            int fw = Math.max(7, (int) (t * w));
            fillRound(ms, x, y, fw, 7, 3f, 0xFF7722CC);
            fillRound(ms, x + fw - 5, y - 2, 10, 11, 5f, 0xFFDD99FF);
            outlineRound(ms, x + fw - 5, y - 2, 10, 11, 5f, 0xFF9944EE);
            return y + 20;
        }

        private int checkbox(DrawContext ctx, MatrixStack ms, int mx, int my, int x, int y, String label, boolean val) {
            boolean hov = hovI(mx, my, x, y, 14, 14);
            fillRound(ms, x, y, 14, 14, 3f, val ? 0xFF8833CC : 0xFF141424);
            outlineRound(ms, x, y, 14, 14, 3f, val ? 0xFFBB55EE : 0xFF444455);
            if (val) ctx.drawTextWithShadow(textRenderer, "\u00a7f\u2714", x + 2, y + 2, 0xFFFFFFFF);
            ctx.drawTextWithShadow(textRenderer, label, x + 18, y + 3, hov ? 0xFFCCCCDD : 0xFF888899);
            return y + 20;
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn) {
            int ox = ox(), oy = oy();
            String[] mods = {"Hitbox", "Trigger", "ESP"};
            int row = oy + 32;
            for (String mod : mods) {
                if (btn == 0 && hovD(mx, my, ox + 6, row, MW - 36, 22)) { toggle(mod); return true; }
                if (hovD(mx, my, ox + MW - 32, row + 4, 26, 14)) {
                    settingsFor = mod.equals(settingsFor) ? null : mod;
                    return true;
                }
                row += 28;
            }
            if (settingsFor != null) handleSettingsClick(mx, my, ox + MW + 6, oy);
            return super.mouseClicked(mx, my, btn);
        }

        @Override
        public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
            if (settingsFor != null && btn == 0) { handleSettingsClick(mx, my, ox() + MW + 6, oy()); return true; }
            return super.mouseDragged(mx, my, btn, dx, dy);
        }

        private void handleSettingsClick(double mx, double my, int ox, int oy) {
            int py = oy + 28;
            switch (settingsFor) {
                case "Hitbox" -> {
                    int sy = py + 11;
                    if (hovD(mx, my, ox + 8, sy, SP_W - 16, 7))
                        config.xzExpand = round1(MathHelper.clamp((float) ((mx - (ox + 8)) / (SP_W - 16)) * 2.5f + 0.5f, 0.5f, 3.0f));
                    py += 31; sy = py + 11;
                    if (hovD(mx, my, ox + 8, sy, SP_W - 16, 7))
                        config.yExpand = round1(MathHelper.clamp((float) ((mx - (ox + 8)) / (SP_W - 16)) * 2.5f + 0.5f, 0.5f, 3.0f));
                }
                case "Trigger" -> {
                    int sy = py + 11;
                    if (hovD(mx, my, ox + 8, sy, SP_W - 16, 7))
                        config.triggerDelay = (int) MathHelper.clamp(((mx - (ox + 8)) / (SP_W - 16)) * 300, 0, 300);
                    py += 31;
                    if (hovD(mx, my, ox + 8, py, 14, 14)) config.trigBlockShield = !config.trigBlockShield;
                    py += 20;
                    if (hovD(mx, my, ox + 8, py, 14, 14)) config.trigBlockEating = !config.trigBlockEating;
                }
                case "ESP" -> {
                    py += 24;
                    int sy = py + 11;
                    if (hovD(mx, my, ox + 8, sy, SP_W - 16, 7)) config.espR = MathHelper.clamp((float) ((mx - (ox + 8)) / (SP_W - 16)), 0, 1);
                    py += 31; sy = py + 11;
                    if (hovD(mx, my, ox + 8, sy, SP_W - 16, 7)) config.espG = MathHelper.clamp((float) ((mx - (ox + 8)) / (SP_W - 16)), 0, 1);
                    py += 31; sy = py + 11;
                    if (hovD(mx, my, ox + 8, sy, SP_W - 16, 7)) config.espB = MathHelper.clamp((float) ((mx - (ox + 8)) / (SP_W - 16)), 0, 1);
                    py += 31; sy = py + 11;
                    if (hovD(mx, my, ox + 8, sy, SP_W - 16, 7))
                        config.espLineWidth = round1(MathHelper.clamp((float) ((mx - (ox + 8)) / (SP_W - 16)) * 4.5f + 0.5f, 0.5f, 5.0f));
                    py += 31;
                    if (hovD(mx, my, ox + 8, py, 14, 14)) config.espThroughWalls = !config.espThroughWalls;
                }
            }
            config.save();
        }

        private float round1(float v) { return Math.round(v * 10) / 10f; }

        private boolean isOn(String n) {
            return switch (n) {
                case "Hitbox" -> config.hitboxActive;
                case "Trigger" -> config.triggerActive;
                case "ESP" -> config.espActive;
                default -> false;
            };
        }

        private void toggle(String n) {
            switch (n) {
                case "Hitbox" -> config.hitboxActive = !config.hitboxActive;
                case "Trigger" -> config.triggerActive = !config.triggerActive;
                case "ESP" -> config.espActive = !config.espActive;
            }
            config.save();
        }

        @Override
        public void close() {
            config.save();
            super.close();
        }

        private int ox() { return width / 2 - MW / 2 - SP_W / 2 - 3; }
        private int oy() { return height / 2 - MH / 2; }
        private boolean hovI(int mx, int my, int x, int y, int w, int h) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }
        private boolean hovD(double mx, double my, double x, double y, double w, double h) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }
    }
}
