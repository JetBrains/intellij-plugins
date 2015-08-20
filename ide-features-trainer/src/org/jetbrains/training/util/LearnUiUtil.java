package org.jetbrains.training.util;

import com.intellij.ide.navigationToolbar.NavBarRootPaneExtension;
import com.intellij.ide.projectView.impl.ProjectViewTree;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.impl.EditorComposite;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorsSplitters;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.openapi.wm.impl.IdeGlassPaneImpl;
import com.intellij.openapi.wm.impl.IdeRootPane;
import com.intellij.openapi.wm.impl.status.IdeStatusBarImpl;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLayeredPane;
import com.intellij.util.ui.Animator;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.commandsEx.util.XmlUtil;
import org.jetbrains.training.ui.MiniCloseButton;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by karashevich on 27/07/15.
 */
public class LearnUiUtil {

    public enum IdeComponent {
        EDITOR,
        PROJECT_TREE,
        STATUS_BAR,
        NAVIGATION_BAR,
        TOOLBAR
    }

    public static final LearnUiUtil INSTANCE = new LearnUiUtil();

    LearnUiUtil(){

    }

    public static LearnUiUtil getInstance(){
        return INSTANCE;
    }
    
    public void drawIcon(Project project, Editor editor) throws IOException {
        final IdeFrameImpl frame = WindowManagerEx.getInstanceEx().getFrame(project);
        final IdeRootPane ideRootPane = (IdeRootPane)frame.getRootPane();
        final JComponent glassPane = (JComponent) ideRootPane.getGlassPane();

        java.util.List<Component> allComponents = getAllComponents(ideRootPane);

        JBLayeredPane jblp = null;
        for (Component cmp : allComponents) {
            if(cmp instanceof JBLayeredPane) {
                jblp = (JBLayeredPane) cmp;
                break;
            }
        }

        final BufferedImage image = ImageIO.read(MyClassLoader.getInstance().getImageResourceAsStream("secure25.png"));
        JComponent imageComp = new JComponent(){
            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawImage(image, 0, 0, null);
            }
        };
        final double x0 = editor.getComponent().getVisibleRect().getX() + editor.getComponent().getBounds().getWidth() - image.getWidth() - 15;
        final double y0 = editor.getComponent().getVisibleRect().getY() + editor.getComponent().getBounds().getHeight() - image.getHeight() - 15;

        imageComp.setBounds((int) x0, (int) y0, image.getWidth(), image.getHeight());
        assert jblp != null;
        jblp.add(imageComp, 0);
        jblp.revalidate();
        jblp.repaint();
    }

    public void highlightIdeComponent(IdeComponent ic, Project project){

        Component myComponent = null;
        String componentName = null;
        JBColor color = new JBColor(new Color(39, 52, 126), new Color(39, 52, 126));

        final IdeFrameImpl frame = WindowManagerEx.getInstanceEx().getFrame(project);
        final IdeRootPane ideRootPane = (IdeRootPane)frame.getRootPane();
        final JComponent glassPane = (JComponent) ideRootPane.getGlassPane();

        java.util.List<Component> allComponents = getAllComponents(ideRootPane);

        switch (ic){
            case EDITOR:
                for (Component cmp : allComponents) {
                    if (cmp instanceof EditorsSplitters) {
                        myComponent = (JComponent) cmp;
                        componentName = "Editor Area";
                        break;
                    }

                }

                if (myComponent != null) {
                    highlightComponent(myComponent, componentName, ideRootPane, glassPane, color, true);
                }
                break;
            case NAVIGATION_BAR:
                for (Component cmp : allComponents) {
                    if (cmp instanceof NavBarRootPaneExtension.NavBarWrapperPanel) {
                        myComponent = (JComponent) cmp;
                        componentName = "Navigation Bar";
                        break;
                    }

                }
                break;
            case PROJECT_TREE:

                for (Component cmp : allComponents) {
                    if (cmp instanceof ProjectViewTree) {
                        myComponent = (JComponent) cmp;
                        componentName = "Project Tree Area";
                        break;
                    }

                }

                if (myComponent != null){
                    highlightComponent(myComponent, componentName, ideRootPane, glassPane, color, true);
                }
                break;

            case STATUS_BAR:
                for (Component cmp : allComponents) {
                    if (cmp instanceof IdeStatusBarImpl) {
                        myComponent = (JComponent) cmp;
                        componentName = "Status Bar";
                        break;
                    }
                }

                if (myComponent != null) {
                    final Component finalMyComponent = myComponent;
                    HighlightComponent highlightComponent = highlightComponent(finalMyComponent, null, ideRootPane, glassPane, new JBColor(new Color(9, 103, 202, 123), new Color(9, 103, 202, 123)), false);
                    drawArrowFrom(glassPane, finalMyComponent, highlightComponent);
                }

                break;
        }

//        final HighlightComponent highlightComponent = new HighlightComponent(color, componentName);
//
//        if(myComponent != null) {
//            final Point pt = SwingUtilities.convertPoint(myComponent, new Point(0, 0), ideRootPane);
//            highlightComponent.setBounds(pt.x, pt.y, myComponent.getWidth(), myComponent.getHeight());
//            glassPane.add(highlightComponent);
//
//            glassPane.revalidate();
//            glassPane.repaint();
//        } else {
//            return;
//        }
//
//        final Component finalMyComponent = myComponent;
//        myComponent.addComponentListener(new ComponentAdapter() {
//            @Override
//            public void componentHidden(ComponentEvent componentEvent) {
//                glassPane.removeAll();
//            }
//
//            @Override
//            public void componentMoved(ComponentEvent componentEvent) {
//                final Point pt = SwingUtilities.convertPoint(finalMyComponent, new Point(0, 0), ideRootPane);
//                highlightIdeComponent.setBounds(pt.x, pt.y, finalMyComponent.getWidth(), finalMyComponent.getHeight());
//                glassPane.revalidate();
//                glassPane.repaint();
//            }
//
//            @Override
//            public void componentResized(ComponentEvent componentEvent) {
//                final Point pt = SwingUtilities.convertPoint(finalMyComponent, new Point(0, 0), ideRootPane);
//                highlightIdeComponent.setBounds(pt.x, pt.y, finalMyComponent.getWidth(), finalMyComponent.getHeight());
//                glassPane.revalidate();
//                glassPane.repaint();
//            }
//
//            @Override
//            public void componentShown(ComponentEvent componentEvent) {
//                final Point pt = SwingUtilities.convertPoint(finalMyComponent, new Point(0, 0), ideRootPane);
//                highlightIdeComponent.setBounds(pt.x, pt.y, finalMyComponent.getWidth(), finalMyComponent.getHeight());
//                glassPane.revalidate();
//                glassPane.repaint();
//            }
//        });
    }

    private HighlightComponent highlightComponent(Component myComponent, String componentName, final IdeRootPane ideRootPane, final JComponent glassPane, JBColor color, final boolean showCloseButton) {
        final HighlightComponent hc = new HighlightComponent(color, componentName, null, myComponent.getWidth(), showCloseButton);
        hc.setCloseButtonAction(new Runnable() {
            @Override
            public void run() {
                glassPane.remove(hc);
                glassPane.revalidate();
                glassPane.repaint();
            }
        });
        glassPane.setVisible(true);

        final Point pt = SwingUtilities.convertPoint(myComponent, new Point(0, 0), ideRootPane);
        hc.setBounds(pt.x, pt.y, myComponent.getWidth(), myComponent.getHeight());
        glassPane.add(hc);
        glassPane.revalidate();
        glassPane.repaint();

        final Component finalMyComponent1 = myComponent;
        myComponent.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent componentEvent) {
                glassPane.removeAll();
            }

            @Override
            public void componentMoved(ComponentEvent componentEvent) {
                final Point pt = SwingUtilities.convertPoint(finalMyComponent1, new Point(0, 0), ideRootPane);
                hc.setBounds(pt.x, pt.y, finalMyComponent1.getWidth(), finalMyComponent1.getHeight());
                glassPane.revalidate();
                glassPane.repaint();
            }

            @Override
            public void componentResized(ComponentEvent componentEvent) {
                final Point pt = SwingUtilities.convertPoint(finalMyComponent1, new Point(0, 0), ideRootPane);
                hc.setBounds(pt.x, pt.y, finalMyComponent1.getWidth(), finalMyComponent1.getHeight());
                glassPane.revalidate();
                glassPane.repaint();
            }

            @Override
            public void componentShown(ComponentEvent componentEvent) {
                final Point pt = SwingUtilities.convertPoint(finalMyComponent1, new Point(0, 0), ideRootPane);
                hc.setBounds(pt.x, pt.y, finalMyComponent1.getWidth(), finalMyComponent1.getHeight());
                glassPane.revalidate();
                glassPane.repaint();
            }
        });

        return hc;
    }

    private void drawArrowFrom(final JComponent glassPane, final Component finalMyComponent, @Nullable final HighlightComponent highlightComponent) {
        final Point2D p1 = new Point2D.Double(finalMyComponent.getLocation().getX() + 30.0d, finalMyComponent.getLocation().getY());
        final Point2D pc = new Point2D.Double(finalMyComponent.getLocation().getX() + 30.0d, finalMyComponent.getLocation().getY() - 30.0d);
        final Point2D p0 = new Point2D.Double(finalMyComponent.getLocation().getX() + 60.0d, finalMyComponent.getLocation().getY() - 60.0d);

        final MyArrow.LineArrow myArrow = new MyArrow.LineArrow(p0, pc, p1, Color.BLACK);
        final Point2D.Double[] pointPath = myArrow.getPointPath();

        final MyArrow.LineArrow growingArrow = new MyArrow.LineArrow(p0, pc, p1, Color.BLACK);
        final JComponent jcomp = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                growingArrow.setArrow(
                        new Point2D.Double(finalMyComponent.getLocation().getX() + 60.0d, finalMyComponent.getLocation().getY() - 60.0d),
                        new Point2D.Double(finalMyComponent.getLocation().getX() + 30.0d, finalMyComponent.getLocation().getY() - 30.0d),
                        new Point2D.Double(finalMyComponent.getLocation().getX() + 30.0d, finalMyComponent.getLocation().getY()));
                growingArrow.draw((Graphics2D) graphics);
            }
        };
        jcomp.setBounds(glassPane.getBounds());
        glassPane.add(jcomp);

        final JComponent labelComponent = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.setColor(JBColor.BLACK);
                Font oldFont = g2d.getFont();
                Font font = new Font(oldFont.getName(), Font.BOLD, 14);
                g2d.setFont(font);

                int stringWidth = g2d.getFontMetrics(font).stringWidth("Status Bar");
                final Point2D p = new Point2D.Double(finalMyComponent.getLocation().getX() + 60.0d, finalMyComponent.getLocation().getY() - 60.0d);
                g2d.drawString("Status Bar", (float) p.getX() - 15.0f, (float) p.getY() - 8.0f);
            }
        };

        final MiniCloseButton closeButtonLabel = new MiniCloseButton(new Rectangle((int) p0.getX() + 65, (int) p0.getY() - 25, 12, 12));
        glassPane.add(closeButtonLabel);
        closeButtonLabel.setClickAction(new Runnable() {
            @Override
            public void run() {
                if (highlightComponent != null) {
                    glassPane.remove(highlightComponent);
                }
                glassPane.remove(closeButtonLabel);
                glassPane.remove(labelComponent);
                glassPane.remove(jcomp);
                glassPane.revalidate();
                glassPane.repaint();
            }
        });
//        closeButtonLabel.setBounds(glassPane.getBounds());

        labelComponent.setBounds(glassPane.getBounds());
        glassPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                super.componentResized(componentEvent);
                labelComponent.repaint();
                labelComponent.setBounds(glassPane.getBounds());
                jcomp.setBounds(glassPane.getBounds());
                glassPane.revalidate();
                glassPane.repaint();
            }
        });
        glassPane.add(labelComponent);
        glassPane.revalidate();
        glassPane.repaint();

//        Animator animator = new Animator("Animate arrow", pointPath.length - 3, 500, false) {
//            int j = 3;
//
//            @Override
//            protected void paintCycleEnd() {
//                labelComponent.setBounds(glassPane.getBounds());
//                growingArrow.setArrow(p0, pc, p1);
//                glassPane.add(labelComponent);
//                glassPane.revalidate();
//                glassPane.repaint();
//
//                glassPane.addComponentListener(new ComponentListener() {
//                    @Override
//                    public void componentResized(ComponentEvent componentEvent) {
//                        glassPane.remove(jcomp);
//                        glassPane.remove(labelComponent);
//                    }
//
//                    @Override
//                    public void componentMoved(ComponentEvent componentEvent) {
//                    }
//
//                    @Override
//                    public void componentShown(ComponentEvent componentEvent) {
//                    }
//
//                    @Override
//                    public void componentHidden(ComponentEvent componentEvent) {
//                        glassPane.removeAll();
//                    }
//                });
//            }
//
//            @Override
//            public void paintNow(int i, int i1, int i2) {
//                growingArrow.setArrow(p0, new Point2D.Double((p1.getX() + pointPath[j].getX())/2, (p0.getY() + pointPath[j].getY())/2), pointPath[j]);
//                glassPane.revalidate();
//                glassPane.repaint();
//                j++;
//                j = Math.min(j, pointPath.length - 1);
//            }
//        };

//       animator.reset();
//       animator.resume();

        glassPane.revalidate();
        glassPane.repaint();
    }


    public void getEditorWindow(final Project project){
//        WindowManager.getInstance().getIdeFrame(project).getComponent().setBackground(Color.PINK);
//        EditorWindow currentWindow = FileEditorManagerImpl.getInstanceEx(project).getSplitters().getCurrentWindow();
        final IdeFrameImpl frame = WindowManagerEx.getInstanceEx().getFrame(project);
        final IdeRootPane ideRootPane = (IdeRootPane)frame.getRootPane();

        JComponent editorAreaComponent = null;
        final HighlightComponent myHighlightComponent = new HighlightComponent(new Color(19, 36, 75), "Editor Area", "Layout managers have different strengths and weaknesses. This section discusses some common layout scenarios and which layout managers might work for each scenario. However, once again, it is strongly recommended that you use a builder tool to create your layout managers, such as the NetBeans IDE Matisse GUI builder, rather than coding managers by hand. The scenarios listed below are given for information purposes, in case you are curious about which type of manager is used in different situations, or in case you absolutely must code your manager manually.\n" +
                "\n" +
                "If none of the layout managers we discuss is right for your situation and you cannot use a builder tool, feel free to use other layout managers that you may write or find. Also keep in mind that flexible layout managers such as GridBagLayout and SpringLayout can fulfill many layout needs.\n" +
                "\n", null, false);

//        final JRootPane rootPane = SwingUtilities.getRootPane(component);
        final JComponent glassPane = (JComponent) ideRootPane.getGlassPane();


        final HighlightComponent myHighlightComponent2 = new HighlightComponent(new Color(38, 66, 147), "Project Tree Area", "Here is the description of the component", null, false);
        JComponent componentProjectWindow = null;

        java.util.List<Component> allComponents = getAllComponents(ideRootPane);
        for (Component cmp : allComponents) {
            if (cmp instanceof ProjectViewTree) {
                componentProjectWindow = (JComponent) cmp;
            } else if (cmp instanceof EditorsSplitters) {
                editorAreaComponent = (JComponent) cmp;
            }
        }

        if(componentProjectWindow != null) {
            final Point pt = SwingUtilities.convertPoint(componentProjectWindow, new Point(0, 0), ideRootPane);
            myHighlightComponent2.setBounds(pt.x, pt.y, componentProjectWindow.getWidth(), componentProjectWindow.getHeight());
            glassPane.add(myHighlightComponent2);

            glassPane.revalidate();
            glassPane.repaint();
        }



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
                    mouseEvent.consume();
                } else {
                    Point glassPanePoint = mouseEvent.getPoint();
                    Container container = ideRootPane.getContentPane();
                    JLayeredPane layeredPane = ideRootPane.getLayeredPane();
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

                    IdeFocusManager.findInstance().getFocusOwner();
                    layeredPane.getComponentAt(componentPoint).dispatchEvent(me);
                }


            }

        };
//        glassPane.addMouseListener(mouseListener);
        ((IdeGlassPaneImpl) glassPane).addMousePreprocessor(mouseListener, project);

        if (editorAreaComponent != null) {
            final Point pt = SwingUtilities.convertPoint(editorAreaComponent, new Point(0, 0), ideRootPane);
            myHighlightComponent.setBounds(pt.x, pt.y, editorAreaComponent.getWidth(), editorAreaComponent.getHeight());
            glassPane.add(myHighlightComponent);

            glassPane.revalidate();
            glassPane.repaint();

            final JComponent finalEditorAreaComponent = editorAreaComponent;
            editorAreaComponent.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentHidden(ComponentEvent componentEvent) {
                    glassPane.removeAll();
                }

                @Override
                public void componentMoved(ComponentEvent componentEvent) {
                    final Point pt = SwingUtilities.convertPoint(finalEditorAreaComponent, new Point(0, 0), ideRootPane);
                    myHighlightComponent.setBounds(pt.x, pt.y, finalEditorAreaComponent.getWidth(), finalEditorAreaComponent.getHeight());
                    glassPane.revalidate();
                    glassPane.repaint();
                }

                @Override
                public void componentResized(ComponentEvent componentEvent) {
                    final Point pt = SwingUtilities.convertPoint(finalEditorAreaComponent, new Point(0, 0), ideRootPane);
                    myHighlightComponent.setBounds(pt.x, pt.y, finalEditorAreaComponent.getWidth(), finalEditorAreaComponent.getHeight());
                    glassPane.revalidate();
                    glassPane.repaint();
                }

                @Override
                public void componentShown(ComponentEvent componentEvent) {
                    final Point pt = SwingUtilities.convertPoint(finalEditorAreaComponent, new Point(0, 0), ideRootPane);
                    myHighlightComponent.setBounds(pt.x, pt.y, finalEditorAreaComponent.getWidth(), finalEditorAreaComponent.getHeight());
                    glassPane.revalidate();
                    glassPane.repaint();
                }
            });
        }

    }

    private static class HighlightComponent extends JComponent {
        @NotNull
        private Color myColor;
        @Nullable
        private String myName;
        @Nullable
        private JLabel myLabel;
        @Nullable private JButton myCloseButton;
        @Nullable private JLabel myDescriptionLabel;

        final private int verticalSpace = 12;

        private HighlightComponent(@NotNull final Color c, @Nullable String componentName, @Nullable String description, @Nullable Integer width, boolean showCloseButton) {
            myColor = c;
            myName = componentName;
            BoxLayout layoutManager = new BoxLayout(this, BoxLayout.PAGE_AXIS);
            setLayout(layoutManager);
            if (componentName != null) {

                myLabel = new JLabel();
                myLabel.setText(myName);
                myLabel.setFont(new Font(UIUtil.getLabelFont().getName(), Font.BOLD, 38));
                myLabel.setForeground(Color.WHITE);
                myLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                this.add(Box.createVerticalGlue());
                this.add(myLabel);
                this.add(Box.createRigidArea(new Dimension(0, verticalSpace)));
            } else {
                this.add(Box.createVerticalGlue());
            }
            if (description != null) {

                myDescriptionLabel = new JLabel(XmlUtil.addHtmlTags(description));
                myDescriptionLabel.setForeground(Color.WHITE);
                if (width != null) {
                    myDescriptionLabel.setBorder(new EmptyBorder(0, width / 3, 0, width / 3));
                }
                myDescriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                this.add(myDescriptionLabel);
                this.add(Box.createRigidArea(new Dimension(0, verticalSpace)));

            }
            if (showCloseButton) {
                myCloseButton = new JButton("Got it!");
                myCloseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                this.add(myCloseButton);
            }
            this.add(Box.createVerticalGlue());
        }

        public void setCloseButtonAction(final Runnable runnable){
            if (myCloseButton != null) {
                myCloseButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        runnable.run();
                    }
                });
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;
            Color oldColor = g2d.getColor();
            g2d.setColor(myColor);
            Composite old = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            Rectangle r = getBounds();
            g2d.fillRect(0, 0, r.width, r.height);
            g2d.setColor(myColor.darker());
            g2d.drawRect(0, 0, r.width - 1, r.height - 1);
            g2d.setComposite(old);
            g2d.setColor(oldColor);

        }
    }

    public static java.util.List<Component> getAllComponents(final Container c) {
        Component[] comps = c.getComponents();
        java.util.List<Component> compList = new ArrayList<Component>();
        for (Component comp : comps) {
            compList.add(comp);
            if (comp instanceof Container) {
                compList.addAll(getAllComponents((Container) comp));
            }
        }
        return compList;
    }
}
