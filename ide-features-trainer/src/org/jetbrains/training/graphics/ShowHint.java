package org.jetbrains.training.graphics;

import com.intellij.ide.impl.DataManagerImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;

import java.awt.*;
import java.io.IOException;

/**
 * Created by karashevich on 07/04/15.
 */
public class ShowHint extends AnAction{

    private HintPanel hintPanel;
    private final Dimension DIMENSION = new Dimension(350, 150);

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        final Editor editor = FileEditorManager.getInstance(anActionEvent.getProject()).getSelectedTextEditor();
        showHintPanel(hintPanel, editor, DIMENSION);

    }

    private void showHintPanel(HintPanel hp, Editor editor, Dimension dimension) {

        try {
//            hp = new HintPanel(dimension);
//            hp.setText("<html>· To comment out line use <b>cdm + /</b></html>");
//            hp.show(DIMENSION, computeLocation(editor, DIMENSION));
            String[] strings = {"<html>Use <b>cmd + /</b> to comment out line </html>", "<html>Use <b>cmd + del</b> to delete line </html>"};
            hp = new HintPanel(strings);
            hp.show(DIMENSION, computeLocation(editor, DIMENSION));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        }

    }

    private RelativePoint computeLocation(Editor editor, Dimension dimension){

        final Rectangle visibleRect = editor.getComponent().getVisibleRect();
        final int magicConst = 10;
        Point point = new Point(visibleRect.x + visibleRect.width - dimension.width - magicConst,
                visibleRect.y + magicConst);
        return new RelativePoint(editor.getComponent(), point);
    }


}
