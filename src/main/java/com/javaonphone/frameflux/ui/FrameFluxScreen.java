package com.javaonphone.frameflux.ui;

import com.javaonphone.frameflux.modrinth.ModEntry;
import com.javaonphone.frameflux.modrinth.ModrinthClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class FrameFluxScreen extends Screen {
    private final Screen parent;
    private List<ModEntry> mods;
    private boolean loading = false;

    public FrameFluxScreen(Screen parent) {
        super(Text.literal("FrameFlux"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Загрузить моды"), b -> {
            loading = true;
            new Thread(() -> {
                mods = ModrinthClient.searchOptimizationMods("1.21.11");
                loading = false;
            }).start();
        }).dimensions(width / 2 - 100, height / 2 - 20, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> {
            if (client != null) client.setScreen(parent);
        }).dimensions(width / 2 - 50, height - 30, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);

        if (loading) {
            ctx.drawCenteredTextWithShadow(textRenderer, "Ищем моды...",
                    width / 2, height / 2 + 10, 0xAAAAAA);
        } else if (mods != null) {
            int y = 50;
            for (ModEntry mod : mods) {
                ctx.drawTextWithShadow(textRenderer, mod.title(), 10, y, 0xFFFFFF);
                y += 14;
                if (y > height - 40) break;
            }
        }
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
