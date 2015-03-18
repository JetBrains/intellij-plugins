package org.jetbrains.training.keymap;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.keymap.*;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * Created by karashevich on 18/03/15.
 */
public class SubKeymapUtil{

    public static String getKeyStrokeTextSub(KeyStroke keyStroke) {
        final String modifiers = getModifiersText(keyStroke.getModifiers());
        final String key = SystemInfo.isMac ? MacKeymapUtil.getKeyText(keyStroke.getKeyCode()) : KeyEvent.getKeyText(keyStroke.getKeyCode());

        return (thinOutModifiersText(modifiers) + key);
    }

    public static String getModifiersText(int modifiers) {
        return KeyEvent.getKeyModifiersText(modifiers);
    }

    /**
     * Adding spaces to keyModifier text
     * "shift+ctrl" -> "shift + ctrl"
     */
    public static String thinOutModifiersText(String modifiersString){
        if (modifiersString.equals("")) return "";

        if (modifiersString.contains("+")){
            String[] modifiers = modifiersString.split("[ \\+]");
            String result = "";
            for (String modifier : modifiers) {
                result += modifier + " + ";
            }
            return result;
        } else {
            return (modifiersString + " + ");
        }
    }
}
