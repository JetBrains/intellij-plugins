package training.keymap;


        import com.intellij.openapi.actionSystem.KeyboardShortcut;
        import com.intellij.openapi.actionSystem.Shortcut;
        import com.intellij.openapi.keymap.KeymapManager;
        import org.jetbrains.annotations.Nullable;

        import javax.swing.*;

/**
 * Created by karashevich on 18/03/15.
 */
public class KeymapUtil {


    /**
     *
     * @param actionId
     * @return null if actionId is null
     */
    @Nullable
    public static KeyStroke getShortcutByActionId(@Nullable String actionId){

        final Shortcut[] shortcuts = KeymapManager.getInstance().getActiveKeymap().getShortcuts(actionId);
        KeyStroke keyStroke = null;
        for (Shortcut each : shortcuts) {
            if (each instanceof KeyboardShortcut) {
                KeyboardShortcut keyboardShortcut = (KeyboardShortcut) each;
                keyStroke = keyboardShortcut.getFirstKeyStroke();
                if (keyboardShortcut != null && KeymapManager.getInstance().getActiveKeymap().getConflicts(actionId, keyboardShortcut) == null) break;
            }
        }

        if (keyStroke == null) return null;
        else return keyStroke;

    }

}
