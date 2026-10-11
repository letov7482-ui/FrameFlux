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

    // Виджеты для контента (создаются в init для активной вкладки)
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

        // Кнопка загрузки
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
        // Удаляем старые контентные виджеты
        for (ButtonWidget w : contentWidgets) {
            remove(w);
        }
        contentWidgets.clear();

        // Кнопка загрузки — только на вкладке "Оптимизация"
        if (loadButton != null) {
            loadButton.visible = (currentTab == 0 && !cachedLoading && cachedMods == null && cachedError == null);
        }

        int contentX = SIDEBAR_WIDTH + 10;
        int y = 100;

        switch (currentTab) {
            case 0 -> {
                if (cachedLoading) {
                    addContentLabel("Ищем моды...", contentX, y, 0xFFFFAA);
                } else if (cachedError != null) {
                    addContentLabel("Ошибка: " + cachedError, contentX, y, 0xFF5555);
                } else if (cachedMods != null && !cachedMods.isEmpty()) {
                    addContentLabel("Найдено: " + cachedMods.size() + " модов", contentX, y, 0x55FF55);
                    y += 20;
                    int limit = Math.min(cachedMods.size(), 15);
                    for (int i = 0; i < limit; i++) {
                        ModEntry mod = cachedMods.get(i);
                        ButtonWidget entry = ButtonWidget.builder(
                                Text.literal(mod.title()),
                                b -> {} // заглушка, потом добавим скачивание
                        ).dimensions(contentX, y, 200, 16).build();
                        addDrawableChild(entry);
                        contentWidgets.add(entry);
                        y += 18;
                    }
                } else if (cachedMods != null) {
                    addContentLabel("Моды не найдены", contentX, y, 0xAAAAAA);
                }
            }
            case 1 -> {
                addContentLabel("F - открыть меню", contentX, y, 0xCCCCCC);
                addContentLabel("G - вкл/выкл FrameGen", contentX, y + 16, 0xCCCCCC);
                addContentLabel("FrameGen удваивает FPS", contentX, y + 40, 0xCCCCCC);
            }
            case 2 -> {
                addContentLabel("FrameFlux v1.0.0", contentX, y, 0xCCCCCC);
                addContentLabel("Автор: JavaOnPhone team", contentX, y + 16, 0xCCCCCC);
                addContentLabel("Minecraft 1.21.11 / Fabric", contentX, y + 40, 0xCCCCCC);
            }
        }
    }

    // Вместо drawText — создаём "кнопку-метку" (некликабельную)
    private void addContentLabel(String text, int x, int y, int color) {
        ButtonWidget label = ButtonWidget.builder(Text.literal(text), b -> {})
                .dimensions(x, y, 200, 14).build();
        label.active = false; // визуально отключена, но текст видно
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
