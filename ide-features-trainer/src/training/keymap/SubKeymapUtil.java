package training.keymap;

import com.intellij.openapi.keymap.*;
import com.intellij.openapi.util.SystemInfo;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * Created by karashevich on 18/03/15.
 */
public class SubKeymapUtil{

    public static String getKeyStrokeTextSub(KeyStroke keyStroke) {
        if (keyStroke == null) return "";
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
     * if it is Mac replace "+" from modifiers
     */
    public static String thinOutModifiersText(String modifiersString){
        if (modifiersString.equals("")) return "";

        if (SystemInfo.isMac) {
            if (modifiersString.contains("+")) {
                String[] modifiers = modifiersString.split("[ \\+]");
                String result = "";
                for (String modifier : modifiers) {
                    result += modifier;
                }
                return result;
            } else {
                return (modifiersString);
            }
        } else {
            if (modifiersString.contains("+")) {
                String[] modifiers = modifiersString.split("[ \\+]");
                String result = "";
                for (String modifier : modifiers) {
                    result += modifier + " + ";
                }
                return replaceSpacesWithNonBreakSpace(result);
            } else {
                return replaceSpacesWithNonBreakSpace(modifiersString + " + ");
            }
        }
    }

    private static String replaceSpacesWithNonBreakSpace(String input){
        String nbsp_str = "\u00A0";
        return input.replace(" ", nbsp_str);
    }
}
