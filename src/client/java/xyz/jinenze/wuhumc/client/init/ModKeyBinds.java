package xyz.jinenze.wuhumc.client.init;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeyBinds {
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.GAMEPLAY;

    public static final KeyBinding CHARGE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wuhumc.charge", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, CATEGORY));

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register((client) -> {
            if (CHARGE_KEY.isPressed()) {

            }
        });
    }
}
