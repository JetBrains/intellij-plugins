package org.jetbrains.training.util;

import com.intellij.ide.impl.DataManagerImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.training.graphics.HintPanel;
import org.jetbrains.training.lesson.Lesson;

import java.awt.*;
import java.io.IOException;

/**
 * Created by karashevich on 07/04/15.
 */
public class ShowHint extends AnAction{

    private HintPanel hintPanel;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        final Editor editor = FileEditorManager.getInstance(anActionEvent.getProject()).getSelectedTextEditor();
        showHintPanel(hintPanel, editor);

    }

    public static void showHintPanel(HintPanel hp, Editor editor) {

        try {
//            hp = new HintPanel(dimension);
//            hp.setText("<html>· To comment out line use <b>cdm + /</b></html>");
//            hp.show(DIMENSION, computeLocation(editor, DIMENSION));
            String[] strings = {"<html>Use <b>cmd + /</b> to comment out line and the same shortcut to uncomment </html> ", "<html>Use <b>cmd + del</b> to delete line </html>", "<html>Use <b>cmd + D</b> to duplicate line </html>"};
            hp = new HintPanel(strings);
            hp.show(computeLocation(editor, hp.getPreferredSize()));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method creates non-disposable hint panel. To create disposable hint panel please use showHintPanel(Lesson lesson, ...)
     * @param editor
     * @param text
     */
    public static void showHintPanel(Editor editor, String text) {

        try {
            HintPanel hp = new HintPanel(new String[]{text});
            hp.show(computeLocation(editor, hp.getPreferredSize()));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method creates non-disposable hint panel. To create disposable hint panel please use showHintPanel(Lesson lesson, ...)
     * @param editor
     * @param text
     */
    public static void showHintPanel(Lesson lesson, Editor editor, String text) {

        try {
            lesson.hintPanel = new HintPanel(new String[]{text});
            lesson.hintPanel.show(computeLocation(editor, lesson.hintPanel.getPreferredSize()));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        }

    }

    private static RelativePoint computeLocation(Editor editor, Dimension dimension){

        final Rectangle visibleRect = editor.getComponent().getVisibleRect();
        final int magicConst = 10;
        Point point = new Point(visibleRect.x + visibleRect.width - dimension.width - magicConst,
                visibleRect.y + magicConst);
        return new RelativePoint(editor.getComponent(), point);
    }


}
