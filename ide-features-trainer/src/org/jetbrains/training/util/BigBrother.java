package org.jetbrains.training.util;


import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import shortcutter.WrongShortcutException;

/**
 * Created by karashevich on 15/04/15.
 */
public class BigBrother{

    public static final BigBrother INSTANCE = new BigBrother();
    private SmallLog mySmallLog;

    public static BigBrother getInstance(){
        return INSTANCE;
    }

    public BigBrother() {
        if (mySmallLog == null)
            try {
                mySmallLog = new SmallLog();
            } catch (WrongShortcutException e) {
                e.printStackTrace();
            }
        ActionManager.getInstance().addAnActionListener(new AnActionListener() {

            @Override
            public void beforeActionPerformed(AnAction anAction, DataContext dataContext, AnActionEvent anActionEvent) {

                final String actionId = ActionManager.getInstance().getId(anAction);
//                final KeyStroke shortcutByActionId = KeymapUtil.getShortcutByActionId(actionId);
//                final String shortcutText = SubKeymapUtil.getKeyStrokeTextSub(shortcutByActionId);
                final String shortcutText = "<shortcut>";
                if (actionId != null) {
                    mySmallLog.addLine(SmallLog.ACTION + actionId.toString() + " (" + shortcutText + ")");
                }
            }

            @Override
            public void afterActionPerformed(AnAction anAction, DataContext dataContext, AnActionEvent anActionEvent) {
            }

            @Override
            public void beforeEditorTyping(char c, DataContext dataContext) {
                mySmallLog.addChar(c);
            }
        });
    }



}
