package com.javaonphone.frameflux.ui;

import com.javaonphone.frameflux.modrinth.ModEntry;
import com.javaonphone.frameflux.modrinth.ModrinthClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class FrameFluxScreen extends Screen {
    // СТАТИЧЕСКИЙ КЭШ — сохраняется между пересозданиями экрана
    private static List<ModEntry> cachedMods = null;
    private static String cachedError = null;
    private static boolean cachedLoading = false;

    private final Screen parent;
    private int currentTab;

    private ButtonWidget loadButton;

    private static final int SIDEBAR_WIDTH = 110;
    private static final int TAB_HEIGHT = 24;
    private static final int TAB_START_Y = 40;

    public FrameFluxScreen(Screen parent) {
        this(parent, 0);
    }

    public FrameFluxScreen(Screen parent, int initialTab) {
        super(Text.literal("FrameFlux"));
        this.parent = parent;
        this.currentTab = initialTab;
    }

    @Override
    protected void init() {
        int tabX = 10;
        int tabY = TAB_START_Y;

        addDrawableChild(ButtonWidget.builder(Text.literal("Оптимизация"), b -> {
            currentTab = 0;
            updateVisibility();
        }).dimensions(tabX, tabY, SIDEBAR_WIDTH - 20, TAB_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Помощь"), b -> {
            currentTab = 1;
            updateVisibility();
        }).dimensions(tabX, tabY + TAB_HEIGHT + 4, SIDEBAR_WIDTH - 20, TAB_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("О моде"), b -> {
            currentTab = 2;
            updateVisibility();
        }).dimensions(tabX, tabY + (TAB_HEIGHT + 4) * 2, SIDEBAR_WIDTH - 20, TAB_HEIGHT).build());

        loadButton = ButtonWidget.builder(Text.literal("Загрузить моды"), b -> {
            cachedLoading = true;
            cachedError = null;
            cachedMods = null;
            updateVisibility();
            new Thread(() -> {
                List<ModEntry> result = null;
                String error = null;
                try {
                    result = ModrinthClient.searchOptimizationMods("1.21.11");
                } catch (Exception e) {
                    error = e.getMessage() != null ? e.getMessage() : "Неизвестная ошибка";
                }
                cachedMods = result;
                cachedError = error;
                cachedLoading = false;
                // Пересоздаём экран в главном потоке
                if (client != null) {
                    client.execute(() -> client.setScreen(new FrameFluxScreen(parent, 0)));
                }
            }).start();
        }).dimensions(SIDEBAR_WIDTH + 10, 70, 160, 20).build();

        addDrawableChild(loadButton);
        updateVisibility();
    }

    private void updateVisibility() {
        if (loadButton != null) {
            loadButton.visible = (currentTab == 0 && !cachedLoading);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF);
        ctx.fill(0, 30, SIDEBAR_WIDTH, height, 0xAA1A1A1A);

        int activeY = TAB_START_Y + currentTab * (TAB_HEIGHT + 4);
        ctx.fill(5, activeY - 2, SIDEBAR_WIDTH - 5, activeY + TAB_HEIGHT + 2, 0x55FFAA00);

        switch (currentTab) {
            case 0 -> renderOptimizeTab(ctx);
            case 1 -> renderHelpTab(ctx);
            case 2 -> renderAboutTab(ctx);
        }
    }

    private void renderOptimizeTab(DrawContext ctx) {
        int contentX = SIDEBAR_WIDTH + 10;
        int y = 100;

        if (cachedLoading) {
            ctx.drawText(textRenderer, "Ищем моды...", contentX, y, 0xFFFFAA, false);
            return;
        }

        if (cachedError != null) {
            ctx.drawText(textRenderer, "Ошибка: " + cachedError, contentX, y, 0xFF5555, false);
            return;
        }

        if (cachedMods != null && !cachedMods.isEmpty()) {
            ctx.drawText(textRenderer, "Найдено: " + cachedMods.size() + " модов",
                    contentX, y, 0x55FF55, false);
            y += 18;

            // Рисуем простым drawText (без тени — на GL4ES тень иногда ломает вывод)
            for (ModEntry mod : cachedMods) {
                if (y > height - 20) break;
                ctx.drawText(textRenderer, "> " + mod.title(), contentX, y, 0xFFFFFF, false);
                y += 12;
            }
        } else if (cachedMods != null) {
            ctx.drawText(textRenderer, "Моды не найдены", contentX, y, 0xAAAAAA, false);
        }
    }

    private void renderHelpTab(DrawContext ctx) {
        int contentX = SIDEBAR_WIDTH + 10;
        int y = 70;
        String[] lines = {
                "F - открыть меню",
                "G - вкл/выкл FrameGen",
                "",
                "FrameGen удваивает FPS"
        };
        for (String line : lines) {
            ctx.drawText(textRenderer, line, contentX, y, 0xCCCCCC, false);
            y += 14;
        }
    }

    private void renderAboutTab(DrawContext ctx) {
        int contentX = SIDEBAR_WIDTH + 10;
        int y = 70;
        String[] lines = {
                "FrameFlux v1.0.0",
                "Автор: JavaOnPhone team",
                "",
                "Sodium / Iris",
                "Minecraft 1.21.11 / Fabric"
        };
        for (String line : lines) {
            ctx.drawText(textRenderer, line, contentX, y, 0xCCCCCC, false);
            y += 14;
        }
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
