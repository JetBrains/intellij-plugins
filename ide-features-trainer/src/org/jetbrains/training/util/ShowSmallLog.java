package org.jetbrains.training.util;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.ui.awt.RelativePoint;
import shortcutter.WrongShortcutException;

import java.awt.*;

/**
 * Created by karashevich on 07/04/15.
 */
public class ShowSmallLog extends AnAction{

    private SmallLog smallLog;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        BigBrother.getInstance();
    }

    private static RelativePoint computeLocation(Editor editor, Dimension dimension){

        final Rectangle visibleRect = editor.getComponent().getVisibleRect();
        final int magicConst = 10;
        Point point = new Point(visibleRect.x + visibleRect.width - dimension.width - magicConst,
                visibleRect.y + magicConst);
        return new RelativePoint(editor.getComponent(), point);
    }

    public static void main(String[] args) throws WrongShortcutException {
        new SmallLog();
    }


}
