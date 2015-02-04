package org.jetbrains.training.commands.util;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;

import java.awt.event.InputEvent;

/**
 * Created by karashevich on 04/02/15.
 */
public class TraverseProcessor {

    final private Editor editor;
    final private int destinationOfsset;
    final private AnActionEvent anActionEvent;

    public TraverseProcessor(Editor editor, int destinationOfsset, AnActionEvent anActionEvent) {
        this.editor = editor;
        this.destinationOfsset = destinationOfsset;
        this.anActionEvent = anActionEvent;
    }

    public void process() {
        //Try to replace with invokeLater
        ApplicationManager.getApplication().invokeLater(new TraverseRunnable());
    }


    private class TraverseRunnable implements Runnable {
        @Override
        public void run() {
            System.err.println("Current offset is:" + editor.getCaretModel().getOffset());
            if (editor.getCaretModel().getOffset() != destinationOfsset) {
                if (editor.getCaretModel().getVisualLineEnd() < destinationOfsset) {
                    System.err.println("Editor down");
                    performAction("EditorDown", anActionEvent, new TraverseRunnable());
                } else if (editor.getCaretModel().getVisualLineStart() > destinationOfsset) {
                    //Move caret up
                    System.err.println("Editor up");
                    performAction("EditorUp", anActionEvent, new TraverseRunnable());
                } else {
                    final int j = editor.getCaretModel().getOffset();
                    System.err.print("Traverse in line ");
                    //traverse caret inside
                    if (j > destinationOfsset) {
                        System.err.println("left");
                        editor.getCaretModel().moveToOffset(j - 1);
                        (new TraverseRunnable()).run();
                    } else if (j < destinationOfsset) {
                        System.err.println("right");
                        editor.getCaretModel().moveToOffset(j + 1);
                        (new TraverseRunnable()).run();
                    }
                }
            } else {

            }
        }
    };

    private void performAction(String actionName, final AnActionEvent e, final Runnable runnable) {
        final ActionManager am = ActionManager.getInstance();
        final AnAction targetAction = am.getAction(actionName);
        final InputEvent inputEvent = PerformActionUtil.getInputEvent(actionName);

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                WriteCommandAction.runWriteCommandAction(e.getProject(), new Runnable() {
                    @Override
                    public void run() {
                        am.tryToExecute(targetAction, inputEvent, null, null, false).doWhenDone(new TraverseRunnable());
                    }
                });
            }
        });
    }

    private void runCommand() {
        System.err.println("Comleted!");
    }
}
