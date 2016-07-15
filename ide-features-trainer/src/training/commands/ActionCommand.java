package training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import org.jdom.Element;
import training.keymap.KeymapUtil;
import training.keymap.SubKeymapUtil;
import training.util.PerformActionUtil;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

import static training.util.PerformActionUtil.performAction;

/**
 * Created by karashevich on 30/01/15.
 */
public class ActionCommand extends Command {

    public ActionCommand() {
        super(CommandType.ACTION);
    }

    public final static String SHORTCUT = "<shortcut>";


    @Override
    public void execute(ExecutionList executionList) throws InterruptedException, ExecutionException, BadCommandException {

        final Element element = executionList.getElements().poll();
        final Editor editor = executionList.getEditor();
        final Project project = executionList.getProject();

//        updateHTMLDescription(element.getText(), );

        final String actionType = (element.getAttribute("action").getValue());

        if (element.getAttribute("balloon") != null) {

            final String balloonText = (element.getAttribute("balloon").getValue());

            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        int delay = 0;
                        if (element.getAttribute("delay") != null) {
                            delay = Integer.parseInt(element.getAttribute("delay").getValue());
                        }
                        showBalloon(editor, balloonText, project, delay, actionType, new Runnable(){
                            @Override
                            public void run() {
                                startNextCommand(executionList);
                            }
                        });
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        } else {
            PerformActionUtil.performAction(actionType, editor, project, new Runnable() {
                @Override
                public void run() {
                    startNextCommand(executionList);
                }
            });
        }
    }

    /**
     * @param editor - editor where to show balloon, also uses for locking while balloon appearing
     */
    private static void showBalloon(final Editor editor, String text, final Project project, final int delay, final String actionType, final Runnable runnable) throws InterruptedException {
        FileEditorManager instance = FileEditorManager.getInstance(project);
        if (instance == null) return;
        if (editor == null) return;

        int offset = editor.getCaretModel().getCurrentCaret().getOffset();
        VisualPosition position = editor.offsetToVisualPosition(offset);
        Point point = editor.visualPositionToXY(position);

        String balloonText = text;

        if (actionType != null) {
            final KeyStroke shortcutByActionId = KeymapUtil.getShortcutByActionId(actionType);
            final String shortcutText = SubKeymapUtil.getKeyStrokeTextSub(shortcutByActionId);
            balloonText = substitution(balloonText, shortcutText);
        }

        BalloonBuilder builder =
                JBPopupFactory.getInstance().
                        createHtmlTextBalloonBuilder(balloonText, null, UIUtil.getLabelBackground(), null)
                        .setHideOnClickOutside(false)
                        .setCloseButtonEnabled(true)
                        .setHideOnKeyOutside(false);
        final Balloon myBalloon = builder.createBalloon();

        myBalloon.show(new RelativePoint(editor.getContentComponent(), point), Balloon.Position.above);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                    myBalloon.hide();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

            }
        }).start();

        myBalloon.addListener(new JBPopupListener() {
            @Override
            public void beforeShown(LightweightWindowEvent lightweightWindowEvent) {

            }

            @Override
            public void onClosed(LightweightWindowEvent lightweightWindowEvent) {
//                performMyAction(elements, lesson, editor, e, document, target, infoPanel, actionType);
                WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                    @Override
                    public void run() {
                        try {

                            performAction(actionType, editor, project, runnable);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        } catch (ExecutionException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public static String substitution(String text, String shortcutString){
        if (text.contains(SHORTCUT)) {
            return text.replace(SHORTCUT, shortcutString);
        } else {
            return text;
        }
    }


}



