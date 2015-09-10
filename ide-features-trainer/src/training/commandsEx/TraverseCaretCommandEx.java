package training.commandsEx;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import org.jdom.Element;
import training.editor.MouseListenerHolder;
import training.lesson.Lesson;
import training.commandsEx.util.PerformActionUtil;
import training.graphics.DetailPanel;

import java.awt.event.InputEvent;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 30/01/15.
 */
public class TraverseCaretCommandEx extends CommandEx {

    public TraverseCaretCommandEx(){
        super(CommandType.TRAVERSECARET);
    }

    @Override
    public void execute(final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, final Document document, final String target, final DetailPanel infoPanel, final MouseListenerHolder mouseListenerHolder) throws InterruptedException, ExecutionException {

        Element element = elements.poll();
        updateDescription(element, infoPanel, editor);

        int delay = 100;

        final String stopString = (element.getAttribute("stop").getValue());
        final int stop = Integer.parseInt(stopString);

        if (element.getAttribute("delay") != null) {
            delay = Integer.parseInt(element.getAttribute("delay").getValue());
        }

        TraverseProcessor traverseProcessor = new TraverseProcessor(editor, stop, e, delay) {
            @Override
            public void runCommand() {
                startNextCommand(elements, lesson, editor, e, document, target ,infoPanel, mouseListenerHolder);
            }
        };

        traverseProcessor.process();

    }


    private abstract class TraverseProcessor{

        final private Editor editor;
        final private int destinationOfsset;
        final private AnActionEvent anActionEvent;
        final int delay;

        public TraverseProcessor(Editor editor, int destinationOfsset, AnActionEvent anActionEvent, int delay) {
            this.editor = editor;
            this.destinationOfsset = destinationOfsset;
            this.anActionEvent = anActionEvent;
            this.delay = delay;
        }

        public void process() {
            //Try to replace with invokeLater
            ApplicationManager.getApplication().invokeLater(new TraverseRunnable());
        }


        private class TraverseRunnable implements Runnable {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
                            ApplicationManager.getApplication().invokeLater(new TraverseRunnable());
                        } else if (j < destinationOfsset) {
                            editor.getCaretModel().moveToOffset(j + 1);
                            ApplicationManager.getApplication().invokeLater(new TraverseRunnable());
                        }
                    }
                } else {
                    runCommand();
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
