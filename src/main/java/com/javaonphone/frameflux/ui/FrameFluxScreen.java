package com.javaonphone.frameflux.ui;

import com.javaonphone.frameflux.modrinth.ModEntry;
import com.javaonphone.frameflux.modrinth.ModrinthClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class FrameFluxScreen extends Screen {
    private static List<ModEntry> cachedMods = null;
    private static String cachedError = null;
    private static boolean cachedLoading = false;

    private final Screen parent;
    private int currentTab;

    private ButtonWidget loadButton;
    private final List<ButtonWidget> contentWidgets = new ArrayList<>();

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
        contentWidgets.clear();

        int tabX = 10;
        int tabY = TAB_START_Y;

        addDrawableChild(ButtonWidget.builder(Text.literal("Оптимизация"), b -> {
            currentTab = 0;
            rebuildContent();
        }).dimensions(tabX, tabY, SIDEBAR_WIDTH - 20, TAB_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Помощь"), b -> {
            currentTab = 1;
            rebuildContent();
        }).dimensions(tabX, tabY + TAB_HEIGHT + 4, SIDEBAR_WIDTH - 20, TAB_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("О моде"), b -> {
            currentTab = 2;
            rebuildContent();
        }).dimensions(tabX, tabY + (TAB_HEIGHT + 4) * 2, SIDEBAR_WIDTH - 20, TAB_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Планы"), b -> {
            currentTab = 3;
            rebuildContent();
        }).dimensions(tabX, tabY + (TAB_HEIGHT + 4) * 3, SIDEBAR_WIDTH - 20, TAB_HEIGHT).build());

        loadButton = ButtonWidget.builder(Text.literal("Загрузить моды"), b -> {
            cachedLoading = true;
            cachedError = null;
            cachedMods = null;
            rebuildContent();
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
                if (client != null) {
                    client.execute(() -> client.setScreen(new FrameFluxScreen(parent, 0)));
                }
            }).start();
        }).dimensions(SIDEBAR_WIDTH + 10, 70, 160, 20).build();

        addDrawableChild(loadButton);
        rebuildContent();
    }

    private void rebuildContent() {
        for (ButtonWidget w : contentWidgets) {
            remove(w);
        }
        contentWidgets.clear();

        if (loadButton != null) {
            loadButton.visible = (currentTab == 0 && !cachedLoading && cachedMods == null && cachedError == null);
        }

        int contentX = SIDEBAR_WIDTH + 10;
        int y = 100;

        switch (currentTab) {
            case 0 -> buildOptimizeTab(contentX, y);
            case 1 -> buildHelpTab(contentX, y);
            case 2 -> buildAboutTab(contentX, y);
            case 3 -> buildPlansTab(contentX, y);
        }
    }

    // === Вкладка "Оптимизация" ===
    private void buildOptimizeTab(int contentX, int y) {
        if (cachedLoading) {
            addLabel("Ищем моды...", contentX, y);
            return;
        }
        if (cachedError != null) {
            addLabel("Ошибка: " + cachedError, contentX, y);
            return;
        }
        if (cachedMods != null && !cachedMods.isEmpty()) {
            addLabel("Найдено: " + cachedMods.size() + " модов", contentX, y);
            y += 20;
            int limit = Math.min(cachedMods.size(), 12);
            for (int i = 0; i < limit; i++) {
                ModEntry mod = cachedMods.get(i);
                ButtonWidget entry = ButtonWidget.builder(
                        Text.literal(mod.title()),
                        b -> {}
                ).dimensions(contentX, y, 220, 16).build();
                addDrawableChild(entry);
                contentWidgets.add(entry);
                y += 18;
            }
        } else if (cachedMods != null) {
            addLabel("Моды не найдены", contentX, y);
        }
    }

    // === Вкладка "Помощь" ===
    private void buildHelpTab(int contentX, int y) {
        String[] lines = {
                "F - открыть меню",
                "G - вкл/выкл FrameGen",
                "",
                "Как работает FrameGen:",
                "1. Мод захватывает кадр",
                "2. Пропускает следующий",
                "3. Вставляет свой кадр",
                "",
                "FPS растёт, GPU отдыхает,",
                "телефон меньше греется.",
                "",
                "Если мигает - выключи G.",
                "На Android depth-текстуры",
                "могут не работать."
        };
        for (String line : lines) {
            addLabel(line, contentX, y);
            y += 16;
        }
    }

    // === Вкладка "О моде" ===
    private void buildAboutTab(int contentX, int y) {
        String[] lines = {
                "FrameFlux v1.0.0",
                "Автор: JavaOnPhone team",
                "",
                "Minecraft: 1.21.11",
                "Загрузчик: Fabric",
                "",
                "Совместимость:",
                "Sodium, Iris, Lithium",
                "",
                "Технологии:",
                "- Репроекция кадров",
                "- Depth-текстура",
                "- JOML матрицы",
                "- OkHttp + Gson",
                "",
                "Лицензия: MIT"
        };
        for (String line : lines) {
            addLabel(line, contentX, y);
            y += 16;
        }
    }

    // === Вкладка "Планы" ===
    private void buildPlansTab(int contentX, int y) {
        String[] lines = {
                "v1.1 - Улучшения:",
                "- GLSL шейдер репроекции",
                "- Motion vectors",
                "- HUD-кэш (+30 FPS)",
                "- Пресеты настроек",
                "",
                "v1.2 - Оптимизация:",
                "- Sodium Optimizer",
                "- Автодетект железа",
                "- Тост-уведомления",
                "",
                "v2.0 - Легенда:",
                "- Экстраполяция (0 lag)",
                "- Умный множитель 2x-4x",
                "- Порт на 1.20.x и NeoForge"
        };
        for (String line : lines) {
            addLabel(line, contentX, y);
            y += 16;
        }
    }

    private void addLabel(String text, int x, int y) {
        ButtonWidget label = ButtonWidget.builder(Text.literal(text), b -> {})
                .dimensions(x, y, 240, 14).build();
        label.active = false;
        addDrawableChild(label);
        contentWidgets.add(label);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF);
        ctx.fill(0, 30, SIDEBAR_WIDTH, height, 0xAA1A1A1A);

        int activeY = TAB_START_Y + currentTab * (TAB_HEIGHT + 4);
        ctx.fill(5, activeY - 2, SIDEBAR_WIDTH - 5, activeY + TAB_HEIGHT + 2, 0x55FFAA00);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
    }
