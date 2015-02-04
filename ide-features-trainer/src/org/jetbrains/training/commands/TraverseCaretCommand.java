package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import org.jdom.Element;
import org.jetbrains.training.Command;
import org.jetbrains.training.CommandFactory;
import org.jetbrains.training.Lesson;
import org.jetbrains.training.commands.util.PerformActionUtil;
import org.jetbrains.training.graphics.DetailPanel;

import java.awt.event.InputEvent;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import static org.jetbrains.training.commands.util.PerformActionUtil.sleepHere;

/**
 * Created by karashevich on 30/01/15.
 */
public class TraverseCaretCommand extends Command {

    public TraverseCaretCommand(){
        super(CommandType.TRAVERSECARET);
    }

    @Override
    public void execute(final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, final Document document, final String target, final DetailPanel infoPanel) throws InterruptedException, ExecutionException {

        Element element = elements.poll();
        updateDescription(element, infoPanel, editor);

        int delay = 20;

        final String stopString = (element.getAttribute("stop").getValue());
        final int stop = Integer.parseInt(stopString);

        if (element.getAttribute("delay") != null) {
            delay = Integer.parseInt(element.getAttribute("delay").getValue());
        }

        TraverseProcessor traverseProcessor = new TraverseProcessor(editor, stop, e) {
            @Override
            public void runCommand() {
                startNextCommand(elements, lesson, editor, e, document, target ,infoPanel);
            }
        };

        traverseProcessor.process();

        //execute next
    }


    private abstract class TraverseProcessor{

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
                if (editor.getCaretModel().getOffset() != destinationOfsset) {
                    if (editor.getCaretModel().getVisualLineEnd() < destinationOfsset) {
                        performAction("EditorDown", anActionEvent, new TraverseRunnable());
                    } else if (editor.getCaretModel().getVisualLineStart() > destinationOfsset) {
                        //Move caret up
                        performAction("EditorUp", anActionEvent, new TraverseRunnable());
                    } else {
                        final int j = editor.getCaretModel().getOffset();
                        //traverse caret inside
                        if (j > destinationOfsset) {
                            editor.getCaretModel().moveToOffset(j - 1);
                            (new TraverseRunnable()).run();
                        } else if (j < destinationOfsset) {
                            editor.getCaretModel().moveToOffset(j + 1);
                            (new TraverseRunnable()).run();
                        }
                    }
                } else {

                }
            }
        }

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

        public abstract void runCommand();

    }
}
