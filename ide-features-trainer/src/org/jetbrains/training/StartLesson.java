package org.jetbrains.training;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Painter;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.*;
import com.intellij.ui.awt.RelativePoint;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.graphics.DetailPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;

/**
 * Created by karashevich on 17/12/14.
 */
public class StartLesson extends AnAction {

    private boolean isRecording = false;
    DetailPanel infoPanel;
    Logger logger;


    public void actionPerformed(final AnActionEvent e) {

        logger = Logger.getInstance("#training-concept.StartLesson");

        try {

            final VirtualFile vf;
            vf = createFile(e.getProject());

            OpenFileDescriptor descriptor = new OpenFileDescriptor(e.getProject(), vf);
            final Editor editor = FileEditorManager.getInstance(e.getProject()).openTextEditor(descriptor, true);
            final Document document = editor.getDocument();

            InputStream is = this.getClass().getResourceAsStream("JavaLessonExample2.java");
            final String target = new Scanner(is).useDelimiter("\\Z").next();

            final Scenario scn = new Scenario("SampleScenario.xml");



            showInfoPanel(editor);
            final Editor editor1 = editor;

            final Thread roboThread = new Thread("RoboThread") {
                @Override
                public void run() {

                    try {

                        if (scn.equals(null)) {
                            System.err.println("Scenario is empty or cannot be read!");
                            return;
                        }
                        if (scn.getRoot().equals(null)) {
                            System.err.println("Scenario is empty or cannot be read!");
                            return;
                        }

                        for (final Element element : scn.getRoot().getChildren()) {
                            if (element.getName().equals("TypeText")) {

                                logger.info("Typing text.");

                                if (element.getAttribute("description") != null) {
                                    final String description = (element.getAttribute("description").getValue().toString());
    //                                            myPainter.setText(description);
                                    infoPanel.setText(description);
                                }

                                if (element.getAttribute("btn") != null) {
                                    final String buttonText =(element.getAttribute("btn").getValue().toString());
                                    infoPanel.setButtonText(buttonText);
                                    infoPanel.addWaitToButton(editor);
                                } else {
                                    infoPanel.hideButton();
                                }

                                final String finalText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
                                boolean isTyping = true;
                                final int[] i = {0};
                                final int initialOffset = editor1.getCaretModel().getOffset();

                                while (isTyping) {
                                    Thread.sleep(20);
                                    final int finalI = i[0];
                                    WriteCommandAction.runWriteCommandAction(e.getProject(), new Runnable() {
                                        @Override
                                        public void run() {
                                            editor1.getDocument().insertString(finalI + initialOffset, finalText.subSequence(i[0], i[0] + 1));
                                            editor1.getCaretModel().moveToOffset(finalI + 1 + initialOffset);
                                        }
                                    });
                                    isTyping = (++i[0] < finalText.length());
                                }
                            } else if(element.getName().equals("Action")) {

                                logger.info("Performing action" + element.getAttribute("action").getValue().toString());

                                if (element.getAttribute("description") != null) {
                                    final String description = (element.getAttribute("description").getValue().toString());
                                    infoPanel.setText(description);
                                    //                                            myPainter.setText(description);
                                }

                                if (element.getAttribute("btn") != null) {
                                    final String buttonText =(element.getAttribute("btn").getValue().toString());
                                    infoPanel.setButtonText(buttonText);
                                    infoPanel.addWaitToButton(editor);
                                } else {
                                    infoPanel.hideButton();
                                }

                                final String actionType = (element.getAttribute("action").getValue().toString());

                                if (element.getAttribute("balloon") != null) {

                                    final String balloonText =(element.getAttribute("balloon").getValue().toString());

                                    synchronized (editor) {
                                        ApplicationManager.getApplication().invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    int delay = 0;
                                                    if (element.getAttribute("delay") != null) {
                                                        delay = Integer.parseInt(element.getAttribute("delay").getValue().toString());
                                                    }
                                                    showBalloon(e, editor1, editor, balloonText, delay);
                                                } catch (InterruptedException e1) {
                                                    e1.printStackTrace();
                                                }
                                            }

                                        });
                                        editor.wait();
                                    }

                                }
                                ApplicationManager.getApplication().invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        performAction(actionType, editor);
                                    }
                                });
                                //To see result
                                Thread.sleep(500);

                            } else if(element.getName().equals("Start")) {
                                    if (element.getAttribute("description") != null) {
                                        final String description =(element.getAttribute("description").getValue().toString());
                                        infoPanel.setText(description);
                                    }
                                    if (element.getAttribute("btn") != null) {
                                        final String buttonText =(element.getAttribute("btn").getValue().toString());
                                        infoPanel.setButtonText(buttonText);
                                        infoPanel.addWaitToButton(editor);
                                    }
                                    synchronized (editor) {
                                        editor.wait();
                                    }
                            } else if(element.getName().equals("Text")) {
                                    if (element.getAttribute("description") != null) {
                                        final String description =(element.getAttribute("description").getValue().toString());
                                        infoPanel.setText(description);
                                    }
                                    if (element.getAttribute("btn") != null) {
                                        final String buttonText =(element.getAttribute("btn").getValue().toString());
                                        infoPanel.setButtonText(buttonText);
                                        infoPanel.addWaitToButton(editor);

                                        synchronized (editor) {
                                            editor.wait();
                                        }
                                    } else {
                                        infoPanel.hideButton();
                                    }

                            } else if(element.getName().equals("MoveCaret")) {


                                final String offsetString =(element.getAttribute("offset").getValue().toString());
                                final int offset = Integer.parseInt(offsetString);


                                WriteCommandAction.runWriteCommandAction(e.getProject(), new Runnable() {
                                    @Override
                                    public void run() {
                                        editor1.getCaretModel().moveToOffset(offset);
                                    }
                                });
                            } else if(element.getName().equals("CopyText")) {

                                if (element.getAttribute("btn") != null) {
                                    final String buttonText = (element.getAttribute("btn").getValue().toString());
                                    infoPanel.setButtonText(buttonText);
                                    infoPanel.addWaitToButton(editor);
                                } else {
                                    infoPanel.hideButton();
                                }

                                final String finalText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
                                WriteCommandAction.runWriteCommandAction(e.getProject(), new Runnable() {
                                    @Override
                                    public void run() {
                                        editor1.getDocument().insertString(0, finalText);
                                    }
                                });

                            } else if(element.getName().equals("Try")) {

                                if (element.getAttribute("description") != null) {
                                    final String description =(element.getAttribute("description").getValue().toString());
                                    infoPanel.setText(description);
                                }

                                if (element.getAttribute("btn") != null) {
                                    final String buttonText =(element.getAttribute("btn").getValue().toString());
                                    infoPanel.setButtonText(buttonText);
                                    infoPanel.addWaitToButton(editor);
                                } else {
                                    infoPanel.hideButton();
                                }

                                final ActionsRecorder recorder = new ActionsRecorder(e.getProject(), document, target);
                                isRecording = true;
                                Disposer.register(recorder, new Disposable() {
                                    @Override
                                    public void dispose() {
                                        isRecording = false;
                                    }
                                });
                                recorder.startRecording(new Runnable() {
                                    @Override
                                    public void run() {
                                        infoPanel.setText("Awesome, now you know how to duplicate lines easily!");
                                        infoPanel.greenalize();
                                    }
                                });

                            } else {

                            }
                        }

                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }

            };
            roboThread.start();


        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JDOMException e1) {
            e1.printStackTrace();
        }
    }

    private void showInfoPanel(Editor editor) {
        final Dimension dimension = new Dimension(500, 60);

        final IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(editor.getProject());
        infoPanel = new DetailPanel(dimension);
        RelativePoint location = computeLocation(ideFrame, dimension);
        Rectangle infoBounds = new Rectangle((int) location.getPoint().getX(), (int) location.getPoint().getY(), (int) dimension.getWidth(),(int) dimension.getHeight());

        BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().createBalloonBuilder(infoPanel);
        balloonBuilder.setHideOnClickOutside(false)
                .setCloseButtonEnabled(false)
                .setHideOnKeyOutside(false)
                .setBorderColor(new Color(0, 0, 0, 0))
                .setDialogMode(false)
                .setHideOnFrameResize(false)
                .setFillColor(new Color(0,0,0,0))
                .setHideOnAction(false);


        Balloon balloon = balloonBuilder.createBalloon();
        balloon.setBounds(infoBounds);

        balloon.show(location, Balloon.Position.above);

    }

    private RelativePoint computeLocation(IdeFrame ideFrame, Dimension dimension){
        int statusBarHeight = ideFrame.getStatusBar().getComponent().getHeight();
        Rectangle visibleRect = ideFrame.getComponent().getVisibleRect();
        Point point = new Point(visibleRect.x + (visibleRect.width - dimension.width) / 2, visibleRect.y + visibleRect.height - dimension.height - statusBarHeight - 20);
        return new RelativePoint(ideFrame.getComponent(), point);
    }

    /**
     *
     * @param e
     * @param editor - editor where to show balloon
     * @param lockEditor - using for suspending typing robot until balloon will have been hidden
     */
    private void showBalloon(AnActionEvent e, Editor editor, final Editor lockEditor, String balloonText, final int delay) throws InterruptedException {
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
                synchronized (lockEditor){
                    lockEditor.notifyAll();
                }
            }

        });

    }

    /**
     * performing internal platform action
      * @param actionName - name of IntelliJ Action. For full list please see http://git.jetbrains.org/?p=idea/community.git;a=blob;f=platform/platform-api/src/com/intellij/openapi/actionSystem/IdeActions.java;hb=HEAD
     * @param lockEditor - using for suspending typing robot until this action will be performed
     */
    private void performAction(String actionName, final Editor lockEditor){
        final ActionManager am = ActionManager.getInstance();
        final AnAction targetAction = am.getAction(actionName);
        final InputEvent inputEvent = getInputEvent(actionName);

        am.tryToExecute(targetAction, inputEvent, null, null, false).doWhenDone(new Runnable() {
            @Override
            public void run() {
                synchronized (lockEditor){
                    lockEditor.notifyAll();
                }
            }
        });

    }

    /**
     * Some util method for <i>performAction</i> method
     * @param actionName - please see it in <i>performAction</i> method
     * @return
     */
    public static InputEvent getInputEvent(String actionName) {
        final Shortcut[] shortcuts = KeymapManager.getInstance().getActiveKeymap().getShortcuts(actionName);
        KeyStroke keyStroke = null;
        for (Shortcut each : shortcuts) {
            if (each instanceof KeyboardShortcut) {
                keyStroke = ((KeyboardShortcut)each).getFirstKeyStroke();
                if (keyStroke != null) break;
            }
        }

        if (keyStroke != null) {
            return new KeyEvent(JOptionPane.getRootFrame(),
                    KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(),
                    keyStroke.getModifiers(),
                    keyStroke.getKeyCode(),
                    keyStroke.getKeyChar(),
                    KeyEvent.KEY_LOCATION_STANDARD);
        } else {
            return new MouseEvent(JOptionPane.getRootFrame(), MouseEvent.MOUSE_PRESSED, 0, 0, 0, 0, 1, false, MouseEvent.BUTTON1);
        }


    }


    @Nullable
    private VirtualFile createFile(final Project project) throws IOException {
        final String fileName = "JavaLessonExample.java";


        Module[] modules = ModuleManager.getInstance(project).getModules();
        final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(modules[0]).getSourceRoots();

        InputStream is = this.getClass().getResourceAsStream(fileName);
        //final String content = new Scanner(is).useDelimiter("\\Z").next();
        final String content = "";

        /*
        Creating new file in Project view;
        copying content from initial file "fileName"
         */

        return ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
            @Override
            @Nullable
            public VirtualFile compute() {
                try {
                    VirtualFile vFile = sourceRoots[0].createChildData(this, fileName);
                    VfsUtil.saveText(vFile, content);
                    return vFile;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

    }

    private class MyPainter implements Painter {
        private final Font myFont = new JLabel().getFont();
        private String myString;
        private Component myComponent;

        public void setText(String s){
            myString = s;
//            myComponent.repaint();
        }

        public MyPainter(Component component) {
            myString = "Deafault message";
            myComponent = component;
        }

        @Override
        public void addListener(Listener listener) {

        }

        @Override
        public boolean needsRepaint() {
            return true;
        }

        @Override
        public void paint(Component component, Graphics2D graphics2D) {
            int w = 500;
            int h = 60;
            int arc = 15;

            int x = component.getWidth()/2 - w/2;
            int y = component.getHeight() - h - 30;

            graphics2D.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
            graphics2D.setColor(new Color(0, 0, 0, 120));
            //graphics2D.drawLine(0, 0, component.getWidth(), component.getHeight());
            graphics2D.fillRoundRect(x, y, w, h, arc, arc);

            graphics2D.setFont(myFont);
            FontMetrics fontMetrics = graphics2D.getFontMetrics();
            Rectangle2D rs = fontMetrics.getStringBounds(myString, graphics2D);

            graphics2D.setColor(new Color(255, 255, 255, 255));
            graphics2D.drawString(myString, x + (w - (int) rs.getWidth())/2, y + (h - (int) rs.getHeight())/2 + fontMetrics.getAscent());
        }

        @Override
        public void removeListener(Listener listener) {

        }
    }
}
