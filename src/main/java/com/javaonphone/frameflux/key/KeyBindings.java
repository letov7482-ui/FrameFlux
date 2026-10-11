package com.javaonphone.frameflux.key;

import com.javaonphone.frameflux.FrameFluxClient;
import com.javaonphone.frameflux.ui.FrameFluxScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyBinding openMenu;
    public static KeyBinding toggleFrameGen;

    public static void register() {
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.frameflux.menu", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F, "category.frameflux"));
        toggleFrameGen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.frameflux.toggle", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G, "category.frameflux"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new FrameFluxScreen(null));
                }
            }
            while (toggleFrameGen.wasPressed()) {
                FrameFluxClient c = FrameFluxClient.getInstance();
                if (c != null) c.setFrameGenEnabled(!c.isFrameGenEnabled());
            }
        });
    }
}
