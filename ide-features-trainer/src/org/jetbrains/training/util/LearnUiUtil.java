package org.jetbrains.training.util;

import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.IdeRepaintManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.IdeGlassPane;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.IdeGlassPaneImpl;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by karashevich on 27/07/15.
 */
public class LearnUiUtil {

    public static final LearnUiUtil INSTANCE = new LearnUiUtil();

    LearnUiUtil(){

    }

    public static LearnUiUtil getInstance(){
        return INSTANCE;
    }

    public void getEditorWindow(final Project project){
//        WindowManager.getInstance().getIdeFrame(project).getComponent().setBackground(Color.PINK);
//        EditorWindow currentWindow = FileEditorManagerImpl.getInstanceEx(project).getSplitters().getCurrentWindow();

        final JComponent component = FileEditorManager.getInstance(project).getSelectedTextEditor().getComponent();
        final HighlightComponent myHighlightComponent = new HighlightComponent(JBColor.GREEN);

        final JRootPane rootPane = SwingUtilities.getRootPane(component);
        final JComponent glassPane = (JComponent) rootPane.getGlassPane();

//        IdeEventQueue.getInstance().addDispatcher(new IdeEventQueue.EventDispatcher() {
//            @Override
//            public boolean dispatch(AWTEvent e) {
//                if (e instanceof MouseEvent && ((MouseEvent) e).getID() == MouseEvent.MOUSE_CLICKED) {
//                    MouseEvent me = (MouseEvent) e;
//                    Component c = me.getComponent();
//                    if (c instanceof IdeGlassPane){
//                        System.out.println("glass pane");
//                    }
//                    System.out.println("dispatcher ->" + c.toString());
//
//                    return (c instanceof IdeGlassPaneImpl);
//                } else return false;
//            }
//        }, project);

        glassPane.requestFocus(false);
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (myHighlightComponent.getBounds().contains(mouseEvent.getPoint())) {
                    mouseEvent.consume();
                }
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                if (myHighlightComponent.getBounds().contains(mouseEvent.getPoint())) {
                    mouseEvent.consume();
                }
            }

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (myHighlightComponent.getBounds().contains(mouseEvent.getPoint())) {
                    System.out.println("MouseListener -> " + mouseEvent.toString());
                    mouseEvent.consume();
                } else {
                    Point glassPanePoint = mouseEvent.getPoint();
                    Container container = rootPane.getContentPane();
                    JLayeredPane layeredPane = rootPane.getLayeredPane();
                    Point containerPoint = SwingUtilities.convertPoint(
                            glassPane,
                            glassPanePoint,
                            container);

//                    Component component2 =
//                            SwingUtilities.getDeepestComponentAt(
//                                    container,
//                                    containerPoint.x,
//                                    containerPoint.y);


                    Point componentPoint = SwingUtilities.convertPoint(glassPane, mouseEvent.getPoint(), layeredPane);
//                    layeredPane.dispatchEvent(new MouseEvent(layeredPane,
                    MouseEvent me = new MouseEvent(layeredPane.getComponentAt(componentPoint),
                            mouseEvent.getID(),
                            mouseEvent.getWhen(),
                            mouseEvent.getModifiers(),
                            componentPoint.x,
                            componentPoint.y,
                            mouseEvent.getClickCount(),
                            mouseEvent.isPopupTrigger());

                    System.out.println(me);
                    IdeFocusManager.findInstance().getFocusOwner();
                    layeredPane.getComponentAt(componentPoint).dispatchEvent(me);
                }


            }

        };
//        glassPane.addMouseListener(mouseListener);
        ((IdeGlassPaneImpl) glassPane).addMousePreprocessor(mouseListener, project);

        final Point pt = SwingUtilities.convertPoint(component, new Point(0, 0), rootPane);
        myHighlightComponent.setBounds(pt.x, pt.y, component.getWidth(), component.getHeight());
        glassPane.add(myHighlightComponent);

        glassPane.revalidate();
        glassPane.repaint();

        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent componentEvent) {
                glassPane.removeAll();
            }

            @Override
            public void componentMoved(ComponentEvent componentEvent) {
                final Point pt = SwingUtilities.convertPoint(component, new Point(0, 0), rootPane);
                myHighlightComponent.setBounds(pt.x, pt.y, component.getWidth(), component.getHeight());
                glassPane.revalidate();
                glassPane.repaint();
            }

            @Override
            public void componentResized(ComponentEvent componentEvent) {
                final Point pt = SwingUtilities.convertPoint(component, new Point(0, 0), rootPane);
                myHighlightComponent.setBounds(pt.x, pt.y, component.getWidth(), component.getHeight());
                glassPane.revalidate();
                glassPane.repaint();
            }

            @Override
            public void componentShown(ComponentEvent componentEvent) {
                final Point pt = SwingUtilities.convertPoint(component, new Point(0, 0), rootPane);
                myHighlightComponent.setBounds(pt.x, pt.y, component.getWidth(), component.getHeight());
                glassPane.revalidate();
                glassPane.repaint();
            }
        });





//        currentWindow.ge
    }

    private static class HighlightComponent extends JComponent {
        private Color myColor;

        private HighlightComponent(@NotNull final Color c) {
            myColor = c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;

            Color oldColor = g2d.getColor();
            g2d.setColor(myColor);
            Composite old = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));

            Rectangle r = getBounds();

            g2d.fillRect(0, 0, r.width, r.height);

            g2d.setColor(myColor.darker());
            g2d.drawRect(0, 0, r.width - 1, r.height - 1);

            g2d.setComposite(old);
            g2d.setColor(oldColor);
        }
    }
}
