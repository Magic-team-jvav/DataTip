package com.cooobird.datatip.internal.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * 读取客户端按键的实时状态，包括界面接管输入时的物理按键状态。
 */
public final class ClientKeyState {
    private ClientKeyState() {
    }

    public static boolean isDown(KeyMapping mapping) {
        if (mapping.isDown()) return true;
        if (mapping.isUnbound()) return false;

        InputConstants.Key boundKey = InputConstants.getKey(mapping.saveString());
        long window = Minecraft.getInstance().getWindow().getWindow();
        if (boundKey.getType() == InputConstants.Type.KEYSYM) {
            return InputConstants.isKeyDown(window, boundKey.getValue());
        }
        if (boundKey.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(window, boundKey.getValue()) == GLFW.GLFW_PRESS;
        }
        return false;
    }
}
