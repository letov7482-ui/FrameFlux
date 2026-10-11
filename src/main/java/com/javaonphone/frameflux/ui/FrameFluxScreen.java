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
    private int currentTab = 0; // 0 = Оптимизация, 1 = Помощь, 2 = О моде

    private List<ModEntry> mods = null;
    private boolean loading = false;
    private String errorMessage = null;

    private ButtonWidget tabOptimize;
    private ButtonWidget tabHelp;
    private ButtonWidget tabAbout;
    private ButtonWidget loadButton;

    private static final int SIDEBAR_WIDTH = 110;
    private static final int TAB_HEIGHT = 24;
    private static final int TAB_START_Y = 40;

    public FrameFluxScreen(Screen parent) {
        super(Text.literal("FrameFlux"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int tabX = 10;
        int tabY = TAB_START_Y;

        tabOptimize = ButtonWidget.builder(Text.literal("Оптимизация"), b -> {
            currentTab = 0;
            updateVisibility();
        }).dimensions(tabX, tabY, SIDEBAR_WIDTH - 20, TAB_HEIGHT).build();

        tabHelp = ButtonWidget.builder(Text.literal("Помощь"), b -> {
            currentTab = 1;
            updateVisibility();
        }).dimensions(tabX, tabY + TAB_HEIGHT + 4, SIDEBAR_WIDTH - 20, TAB_HEIGHT).build();

        tabAbout = ButtonWidget.builder(Text.literal("О моде"), b -> {
            currentTab = 2;
            updateVisibility();
        }).dimensions(tabX, tabY + (TAB_HEIGHT + 4) * 2, SIDEBAR_WIDTH - 20, TAB_HEIGHT).build();

        addDrawableChild(tabOptimize);
        addDrawableChild(tabHelp);
        addDrawableChild(tabAbout);

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
                    errorMessage = e.getMessage() != null ? e.getMessage() : "Неизвестная ошибка";
                } finally {
                    loading = false;
                    if (client != null) {
                        client.execute(() -> loadButton.visible = true);
                    }
                }
            }).start();
        }).dimensions(SIDEBAR_WIDTH + 10, 70, 160, 20).build();

        addDrawableChild(loadButton);
        updateVisibility();
    }

    private void updateVisibility() {
        if (loadButton != null) {
            loadButton.visible = (currentTab == 0 && mods == null && !loading && errorMessage == null);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // ВАЖНО: super.render() СНАЧАЛА — он рисует фон и виджеты
        super.render(ctx, mouseX, mouseY, delta);

        // Заголовок
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF);

        // Боковая панель (фон)
        ctx.fill(0, 30, SIDEBAR_WIDTH, height, 0xAA1A1A1A);

        // Подсветка активной вкладки
        int activeY = TAB_START_Y + currentTab * (TAB_HEIGHT + 4);
        ctx.fill(5, activeY - 2, SIDEBAR_WIDTH - 5, activeY + TAB_HEIGHT + 2, 0x55FFAA00);

        // Контент активной вкладки (ПОВЕРХ super.render)
        switch (currentTab) {
            case 0 -> renderOptimizeTab(ctx);
            case 1 -> renderHelpTab(ctx);
            case 2 -> renderAboutTab(ctx);
        }
    }

    private void renderOptimizeTab(DrawContext ctx) {
        int contentX = SIDEBAR_WIDTH + 10;
        int y = 100;

        if (loading) {
            ctx.drawTextWithShadow(textRenderer, "Ищем моды...", contentX, y, 0xFFFFAA);
        } else if (errorMessage != null) {
            ctx.drawTextWithShadow(textRenderer, "Ошибка: " + errorMessage, contentX, y, 0xFF5555);
            ctx.drawTextWithShadow(textRenderer, "Проверь интернет", contentX, y + 14, 0xAAAAAA);
        } else if (mods != null) {
            if (mods.isEmpty()) {
                ctx.drawTextWithShadow(textRenderer, "Моды не найдены", contentX, y, 0xAAAAAA);
            } else {
                for (ModEntry mod : mods) {
                    if (y > height - 40) break;
                    ctx.drawTextWithShadow(textRenderer, mod.title(), contentX, y, 0xFFFFFF);
                    y += 14;
                }
            }
        }
    }

    private void renderHelpTab(DrawContext ctx) {
        int contentX = SIDEBAR_WIDTH + 10;
        int y = 70;
        String[] lines = {
                "F — открыть меню",
                "G — вкл/выкл FrameGen",
                "",
                "FrameGen удваивает FPS,",
                "вставляя кадры между реальными."
        };
        for (String line : lines) {
            ctx.drawTextWithShadow(textRenderer, line, contentX, y, 0xCCCCCC);
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
                "Sodium · Iris",
                "Minecraft 1.21.11 · Fabric"
        };
        for (String line : lines) {
            ctx.drawTextWithShadow(textRenderer, line, contentX, y, 0xCCCCCC);
            y += 14;
        }
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
        }
