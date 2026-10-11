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
    private int currentTab = 0;

    private List<ModEntry> mods = null;
    private boolean loading = false;
    private String errorMessage = null;
    private ButtonWidget loadButton;

    public FrameFluxScreen(Screen parent) {
        super(Text.literal("FrameFlux"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int tabY = 30;
        int tabWidth = 100;
        int totalTabWidth = tabWidth * 3;
        int tabStartX = (width - totalTabWidth) / 2;

        addDrawableChild(ButtonWidget.builder(Text.literal("Оптимизация"), b -> {
            currentTab = 0;
            updateLoadButton();
        }).dimensions(tabStartX, tabY, tabWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Помощь"), b -> {
            currentTab = 1;
            updateLoadButton();
        }).dimensions(tabStartX + tabWidth, tabY, tabWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("О моде"), b -> {
            currentTab = 2;
            updateLoadButton();
        }).dimensions(tabStartX + tabWidth * 2, tabY, tabWidth, 20).build());

        loadButton = ButtonWidget.builder(Text.literal("Загрузить моды"), b -> {
            loading = true;
            errorMessage = null;
            mods = null;
            loadButton.visible = false;
            new Thread(() -> {
                try {
                    List<ModEntry> result = ModrinthClient.searchOptimizationMods("1.21.11");
                    mods = result;
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                } finally {
                    loading = false;
                    if (client != null) {
                        client.execute(() -> loadButton.visible = true);
                    }
                }
            }).start();
        }).dimensions(width / 2 - 100, 70, 200, 20).build();

        addDrawableChild(loadButton);
        updateLoadButton();
    }

    private void updateLoadButton() {
        if (loadButton != null) {
            loadButton.visible = (currentTab == 0 && mods == null && !loading && errorMessage == null);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);

        int tabY = 30, tabWidth = 100;
        int tabStartX = (width - tabWidth * 3) / 2;
        ctx.fill(tabStartX + currentTab * tabWidth, tabY + 20,
                tabStartX + (currentTab + 1) * tabWidth, tabY + 22, 0xFFFFAA00);

        switch (currentTab) {
            case 0 -> renderOptimizeTab(ctx);
            case 1 -> renderHelpTab(ctx);
            case 2 -> renderAboutTab(ctx);
        }
    }

    private void renderOptimizeTab(DrawContext ctx) {
        int y = 100;
        if (loading) {
            ctx.drawCenteredTextWithShadow(textRenderer, "Ищем моды...", width / 2, y, 0xFFFFAA);
        } else if (errorMessage != null) {
            ctx.drawCenteredTextWithShadow(textRenderer, "Ошибка: " + errorMessage, width / 2, y, 0xFF5555);
        } else if (mods != null) {
            if (mods.isEmpty()) {
                ctx.drawCenteredTextWithShadow(textRenderer, "Моды не найдены", width / 2, y, 0xAAAAAA);
            } else {
                for (ModEntry mod : mods) {
                    if (y > height - 50) break;
                    ctx.drawTextWithShadow(textRenderer, mod.title(), 20, y, 0xFFFFFF);
                    y += 14;
                }
            }
        }
    }

    private void renderHelpTab(DrawContext ctx) {
        int y = 70;
        String[] lines = {"F — открыть меню", "G — вкл/выкл FrameGen", "", "FrameGen удваивает FPS"};
        for (String line : lines) {
            ctx.drawCenteredTextWithShadow(textRenderer, line, width / 2, y, 0xCCCCCC);
            y += 14;
        }
    }

    private void renderAboutTab(DrawContext ctx) {
        int y = 70;
        String[] lines = {"FrameFlux v1.0.0", "Автор: JavaOnPhone team", "", "Sodium · Iris", "Minecraft 1.21.11 · Fabric"};
        for (String line : lines) {
            ctx.drawCenteredTextWithShadow(textRenderer, line, width / 2, y, 0xCCCCCC);
            y += 14;
        }
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
