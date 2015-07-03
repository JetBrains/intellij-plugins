package org.jetbrains.training.commandsEx;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ui.popup.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import org.jdom.Element;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.keymap.KeymapUtil;
import org.jetbrains.training.keymap.SubKeymapUtil;
import org.jetbrains.training.lesson.Lesson;
import org.jetbrains.training.graphics.DetailPanel;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.jetbrains.training.commandsEx.util.PerformActionUtil.performAction;

/**
 * Created by karashevich on 30/01/15.
 */
public class ActionCommandEx extends CommandEx {

    public ActionCommandEx() {
        super(CommandType.ACTION);
    }

    public final static String SHORTCUT = "<shortcut>";

    @Override
    public void execute(final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, final Document document, final String target, final DetailPanel infoPanel, final MouseListenerHolder mouseListenerHolder) throws InterruptedException, ExecutionException {

        final Element element = elements.poll();

        updateHTMLDescription(element, infoPanel, editor);
        updateButton(element, elements, lesson, editor, e, document, target, infoPanel, mouseListenerHolder);

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
                        showBalloon(editor, balloonText, e, delay, actionType, new Runnable(){
                            @Override
                            public void run() {
                                startNextCommand(elements, lesson, editor, e, document, target ,infoPanel, mouseListenerHolder);
                            }
                        });
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        } else {
            performAction(actionType, editor, e, new Runnable() {
                @Override
                public void run() {
                    startNextCommand(elements, lesson, editor, e, document, target, infoPanel, mouseListenerHolder);
                }
            });
        }

    }

    /**
     * @param e
     * @param editor - editor where to show balloon, also uses for locking while balloon appearing
     */
    private static void showBalloon(final Editor editor, String text, final AnActionEvent e, final int delay, final String actionType, final Runnable runnable) throws InterruptedException {
        FileEditorManager instance = FileEditorManager.getInstance(e.getProject());
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
                WriteCommandAction.runWriteCommandAction(e.getProject(), new Runnable() {
                    @Override
                    public void run() {
                        try {
                            performAction(actionType, editor, e, runnable);
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



