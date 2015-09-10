package training.keymap;

import com.intellij.openapi.keymap.MacKeymapUtil;

import javax.swing.*;

/**
 * Created by karashevich on 18/03/15.
 */
public class SubMacKeymapUtil extends MacKeymapUtil {

    public static String getKeyStrokeTextAdvanced(KeyStroke keyStroke) {
        final String modifiers = getModifiersText(keyStroke.getModifiers());
        final String key = getKeyText(keyStroke.getKeyCode());
        return (modifiers + " + " + key);
    }
}
