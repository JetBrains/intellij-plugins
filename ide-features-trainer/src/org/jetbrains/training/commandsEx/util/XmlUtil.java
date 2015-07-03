package org.jetbrains.training.commandsEx.util;

import org.jetbrains.training.keymap.KeymapUtil;
import org.jetbrains.training.keymap.SubKeymapUtil;

import javax.swing.*;

/**
 * Created by karashevich on 19/03/15.
 */
public class XmlUtil {

    public final static String SHORTCUT = "<shortcut>";

    /**
     *
     * @return null if no <shortcut> tag inside text
     */
    public static String substitution(String text, String shortcutString){
        if (text.contains(SHORTCUT)) {
            return text.replace(SHORTCUT, shortcutString);
        } else {
            return text;
        }
    }

    /**
     *
     * replaces in text all <action="actionId"> with correspondent shortcut.
     * @param text
     * @return
     */
    public static String replacement(String text){
        String result = text;
        final String TAG = "action";

        while(result.contains("<" + TAG + "=\"")) {
            int start = result.indexOf("<" + TAG + "=\"");
            int end = result.indexOf("\">", start);

            String value = result.substring((start + 3 + TAG.length()), end);
            String replaceString = "ACTION";
            result = result.substring(0, start) + replaceString + result.substring(end + 2);
        }

        return result;
    }

    public static String replaceWithActionShortcut(String text){
        String result = text;
        final String TAG = "action";

        while(result.contains("<" + TAG + "=\"")) {
            int start = result.indexOf("<" + TAG + "=\"");
            int end = result.indexOf("\">", start);

            String value = result.substring((start + 3 + TAG.length()), end);
            final KeyStroke shortcutByActionId = KeymapUtil.getShortcutByActionId(value);
            String shortcutText;
            if (shortcutByActionId == null) {
                shortcutText = value;
            } else {
                shortcutText = SubKeymapUtil.getKeyStrokeTextSub(shortcutByActionId);
            }

            result = result.substring(0, start) + shortcutText + result.substring(end + 2);
        }

        return result;
    }

    public static String removeHtmlTags(String text){
        final int n = text.length();
        if (n > 12 && text.substring(0, 6).equals("<html>") && (text.substring(n-7, n).equals("</html>")))
            return text.substring(6, n - 7);
        else
            return text;
    }

    public static String addHtmlTags(String text){
        final int n = text.length();
        if (n > 12 && text.substring(0, 6).equals("<html>") && (text.substring(n-7, n).equals("</html>")))
            return text;
        else
            return "<html>" + text + "</html>";
    }


}
