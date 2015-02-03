package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ui.popup.*;
import com.intellij.ui.awt.RelativePoint;
import org.jdom.Element;
import org.jetbrains.training.Command;
import org.jetbrains.training.CommandFactory;
import org.jetbrains.training.Lesson;
import org.jetbrains.training.graphics.DetailPanel;

import java.awt.*;
import java.util.*;

import static org.jetbrains.training.commands.util.PerformActionUtil.performAction;

/**
 * Created by karashevich on 30/01/15.
 */
public class ActionCommand extends Command {

    public ActionCommand() {
        super(CommandType.ACTION);
    }

    @Override
    public void execute(final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, final Document document, final String target, final DetailPanel infoPanel) throws InterruptedException {

        final Element element = elements.poll();

        updateDescription(element, infoPanel, editor);
        updateButton(element, elements, lesson, editor, e, document, target, infoPanel);


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
                                try {
                                        CommandFactory.buildCommand(elements.peek()).execute(elements, lesson, editor, e, document, target, infoPanel);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        });
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        } else {
            performAction(actionType, new Runnable() {
                @Override
                public void run() {
                    try {
                        CommandFactory.buildCommand(elements.peek()).execute(elements, lesson, editor, e, document, target, infoPanel);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }

    }

    private static void performMyAction(final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, final Document document, final String target, final DetailPanel infoPanel, final String actionType) {
        WriteCommandAction.runWriteCommandAction(e.getProject(), new Runnable() {
            @Override
            public void run() {
                performAction(actionType, new Runnable() {
                    @Override
                    public void run() {
                        //execute next
                        try {
                            CommandFactory.buildCommand(elements.peek()).execute(elements, lesson, editor, e, document, target, infoPanel);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    /**
     * @param e
     * @param editor - editor where to show balloon, also uses for locking while balloon appearing
     */
    private static void showBalloon(Editor editor, String balloonText, final AnActionEvent e, final int delay, final String actionType, final Runnable actionCallBack) throws InterruptedException {
        FileEditorManager instance = FileEditorManager.getInstance(e.getProject());
        if (instance == null) return;
        if (editor == null) return;

        int offset = editor.getCaretModel().getCurrentCaret().getOffset();
        VisualPosition position = editor.offsetToVisualPosition(offset);
        Point point = editor.visualPositionToXY(position);
        BalloonBuilder builder =
                JBPopupFactory.getInstance().
                        createHtmlTextBalloonBuilder(balloonText, null, Color.LIGHT_GRAY, null)
                        .setHideOnClickOutside(false).setCloseButtonEnabled(true).setHideOnKeyOutside(false);
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
                        performAction(actionType, actionCallBack);
                    }
                });
            }
        });
    }
}



