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

    // Текущая вкладка
    private int currentTab = 0; // 0 = Оптимизация, 1 = Помощь, 2 = О моде

    // Состояние загрузки
    private List<ModEntry> mods = null;
    private boolean loading = false;
    private String errorMessage = null;

    // Кнопки вкладок (создаются в init)
    private ButtonWidget tabOptimize;
    private ButtonWidget tabHelp;
    private ButtonWidget tabAbout;

    public FrameFluxScreen(Screen parent) {
        super(Text.literal("FrameFlux"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // === Панель вкладок (сверху) ===
        int tabY = 30;
        int tabWidth = 100;
        int totalTabWidth = tabWidth * 3;
        int tabStartX = (width - totalTabWidth) / 2;

        tabOptimize = ButtonWidget.builder(Text.literal("Оптимизация"), b -> {
            currentTab = 0;
            refreshWidgets();
        }).dimensions(tabStartX, tabY, tabWidth, 20).build();

        tabHelp = ButtonWidget.builder(Text.literal("Помощь"), b -> {
            currentTab = 1;
            refreshWidgets();
        }).dimensions(tabStartX + tabWidth, tabY, tabWidth, 20).build();

        tabAbout = ButtonWidget.builder(Text.literal("О моде"), b -> {
            currentTab = 2;
            refreshWidgets();
        }).dimensions(tabStartX + tabWidth * 2, tabY, tabWidth, 20).build();

        addDrawableChild(tabOptimize);
        addDrawableChild(tabHelp);
        addDrawableChild(tabAbout);

        // === Контент текущей вкладки ===
        refreshWidgets();
    }

    private void refreshWidgets() {
        // Удаляем только контентные виджеты (кнопки вкладок не трогаем)
        // В Minecraft 1.21.11 проще пересоздать экран
        // Но чтобы не терять состояние — просто перерисуем через render()
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Фон и виджеты
        super.render(ctx, mouseX, mouseY, delta);

        // Заголовок
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);

        // Подсветка активной вкладки (жирная рамка)
        int tabY = 30;
        int tabWidth = 100;
        int totalTabWidth = tabWidth * 3;
        int tabStartX = (width - totalTabWidth) / 2;
        int activeTabX = tabStartX + currentTab * tabWidth;
        ctx.fill(activeTabX, tabY + 20, activeTabX + tabWidth, tabY + 22, 0xFFFFAA00);

        // === Контент вкладки ===
        switch (currentTab) {
            case 0 -> renderOptimizeTab(ctx);
            case 1 -> renderHelpTab(ctx);
            case 2 -> renderAboutTab(ctx);
        }
    }

    // === Вкладка "Оптимизация" ===
    private void renderOptimizeTab(DrawContext ctx) {
        int y = 70;

        if (loading) {
            ctx.drawCenteredTextWithShadow(textRenderer, "Ищем моды...", width / 2, y, 0xFFFFAA);
            return;
        }

        if (errorMessage != null) {
            ctx.drawCenteredTextWithShadow(textRenderer, "Ошибка: " + errorMessage,
                    width / 2, y, 0xFF5555);
            ctx.drawCenteredTextWithShadow(textRenderer, "Проверь интернет и попробуй снова",
                    width / 2, y + 15, 0xAAAAAA);
            return;
        }

        if (mods == null) {
            // Кнопка "Загрузить моды"
            ButtonWidget loadBtn = ButtonWidget.builder(Text.literal("Загрузить моды"), b -> {
                loading = true;
                errorMessage = null;
                mods = null;
                new Thread(() -> {
                    try {
                        List<ModEntry> result = ModrinthClient.searchOptimizationMods("1.21.11");
                        mods = result;
                    } catch (Exception e) {
                        errorMessage = e.getMessage();
                    } finally {
                        loading = false;
                    }
                }).start();
            }).dimensions(width / 2 - 100, y, 200, 20).build();

            // Убираем старую кнопку если есть и добавляем новую
            clearAndAdd(loadBtn);
            return;
        }

        // Список модов
        int listY = y;
        int maxY = height - 50;
        for (ModEntry mod : mods) {
            if (listY > maxY) break;
            ctx.drawTextWithShadow(textRenderer, mod.title(), 20, listY, 0xFFFFFF);
            listY += 14;
        }

        if (mods.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer, "Моды не найдены",
                    width / 2, y, 0xAAAAAA);
        }
    }

    // === Вкладка "Помощь" ===
    private void renderHelpTab(DrawContext ctx) {
        int y = 70;
        String[] lines = {
                "FrameFlux — генерация кадров",
                "",
                "F — открыть это меню",
                "G — включить/выключить FrameGen",
                "",
                "FrameGen удваивает FPS,",
                "вставляя синтетические кадры",
                "между реальными."
        };
        for (String line : lines) {
            ctx.drawCenteredTextWithShadow(textRenderer, line, width / 2, y, 0xCCCCCC);
            y += 14;
        }
    }

    // === Вкладка "О моде" ===
    private void renderAboutTab(DrawContext ctx) {
        int y = 70;
        String[] lines = {
                "FrameFlux v1.0.0",
                "Автор: JavaOnPhone team",
                "",
                "Совместимость: Sodium, Iris",
                "Minecraft: 1.21.11",
                "Загрузчик: Fabric"
        };
        for (String line : lines) {
            ctx.drawCenteredTextWithShadow(textRenderer, line, width / 2, y, 0xCCCCCC);
            y += 14;
        }
    }

    private void clearAndAdd(ButtonWidget btn) {
        // Удаляем все контентные виджеты и добавляем новый
        // В 1.21.11 проще всего переинициализировать экран
        clearWidgets();
        init();
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
    }
