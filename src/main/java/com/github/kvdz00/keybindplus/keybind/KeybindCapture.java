package com.github.kvdz00.keybindplus.keybind;

import com.github.kvdz00.keybindplus.KeybindPlus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.util.LinkedHashMap;
import java.util.Map;

public final class KeybindCapture {
    private KeybindCapture() {}

    public static Map<String, String> captureAll() {
        Minecraft mc = Minecraft.getMinecraft();
        KeyBinding[] keyBindings = mc.gameSettings.keyBindings;
        Map<String, String> result = new LinkedHashMap<>();

        if (keyBindings != null) {
            for (KeyBinding kb : keyBindings) {
                String actionId = kb.getKeyDescription();
                int code = kb.getKeyCode();
                String keyStr = getKeySaveString(code);
                result.put(actionId, keyStr);
            }
        }

        KeybindPlus.LOGGER.info("Captured {} keybinds", result.size());
        return result;
    }

    public static String getKeySaveString(int keyCode) {
        if (keyCode == 0) return "NONE";
        if (keyCode < 0) {
            return "MOUSE_" + (keyCode + 100);
        }
        String name = Keyboard.getKeyName(keyCode);
        return (name != null && !name.isEmpty()) ? name : String.valueOf(keyCode);
    }

    public static int parseKeyCode(String keyStr) {
        if (keyStr == null || keyStr.trim().isEmpty() || keyStr.equalsIgnoreCase("none") || keyStr.equals("0")) {
            return 0;
        }
        if (keyStr.startsWith("MOUSE_")) {
            try {
                int button = Integer.parseInt(keyStr.substring(6));
                return button - 100;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        try {
            return Integer.parseInt(keyStr.trim());
        } catch (NumberFormatException e) {
            int code = Keyboard.getKeyIndex(keyStr.toUpperCase());
            return code;
        }
    }

    public static String getKeyDisplay(int keyCode) {
        if (keyCode == 0) return "NONE";
        if (keyCode < 0) {
            return "Button " + (keyCode + 101);
        }
        String name = Keyboard.getKeyName(keyCode);
        return name != null ? name : "NONE";
    }
}
