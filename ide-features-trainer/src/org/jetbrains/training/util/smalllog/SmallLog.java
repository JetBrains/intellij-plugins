package org.jetbrains.training.util.smalllog;

import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.util.smalllog.actions.*;
import shortcutter.Shortcutter;
import shortcutter.WrongShortcutException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by karashevich on 14/04/15.
 */
public class SmallLog extends JFrame{

    private static Dimension dimension = new Dimension(300, 600);
    private static Color bck = new Color(54, 54 , 55, 255);
    private static Color frg = new Color(230, 230, 230, 230);
    private static Color transparentColor = new Color(0, 0, 0, 0);

    private SemiTransparentPanel semiTransparentPanel;
    private FrameHolder frameHolder;

    public SmallLog() throws Exception {
        super("SmallLog");
        frameHolder = new FrameHolder(this);
        setAlwaysOnTop(true);


        semiTransparentPanel = new SemiTransparentPanel(dimension, this);

        JBScrollPane jbScrollPane = new JBScrollPane(semiTransparentPanel);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        add(jbScrollPane);
        semiTransparentPanel.setVisible(true);
        setSize(dimension);
        setVisible(true);
    }

    public ArrayList<ClickLabel> getClickLabels(){
        return (ArrayList<ClickLabel>) semiTransparentPanel.clickLabels;
    }

    public Pivot getPivot(){
        return semiTransparentPanel.pivot;
    }

    public FrameHolder getFrameHolder(){
        return frameHolder;
    }


    public void addLine(Type type, String text, @Nullable String shortcutText) throws Exception {
        new AddAction(this, type, text).execute();
    }

    public void addChar(char c) {
        semiTransparentPanel.addToCharBuffer(c);
    }

    public SemiTransparentPanel getSemiTransparentPanel(){
        return semiTransparentPanel;
    }

    public void set(ArrayList<ClickLabel> clickLabels) {
        semiTransparentPanel.removeAll();
        for (ClickLabel clickLabel : clickLabels) {
            semiTransparentPanel.add(clickLabel);
        }
        semiTransparentPanel.clickLabels = clickLabels;
        semiTransparentPanel.update();
    }

    public class SemiTransparentPanel extends JPanel{

        private static final int V_GAP = 2;
        private SmallLog smallLog;
        private int lastClicked = -1;
        private Pivot pivot;

        //clickLabels
        private ArrayList<ClickLabel> clickLabels = new ArrayList<ClickLabel>();

        public Queue<Character> getCharBuffer() {
            return charBuffer;
        }

        private final java.util.Queue<Character> charBuffer = new ArrayBlockingQueue<Character>(1000);

        public SemiTransparentPanel(Dimension dimension1, final SmallLog smallLog) throws WrongShortcutException {

            pivot = new Pivot(-1, null, clickLabels);

            this.smallLog = smallLog;
            setSize(dimension1);
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setAppearance(dimension1);

            this.setFocusable(true);
            this.requestFocus();

            //clear all selection if user pressed ESCAPE
            Shortcutter.register(this, "_ESCAPE", new Runnable() {
                @Override
                public void run() {
                    for (ClickLabel cl : clickLabels) {
                        if (cl.isSelected()) flip(cl);
                    }
                    pivot.move(-1);
                }
            });

            Shortcutter.register(this, "_UP", new Runnable() {
                @Override
                public void run() {
                    if (Shortcutter.isShiftPressed) {
                        if (pivot.getPosition() == -1) {
                            if (clickLabels != null) {
                                pivot.move(clickLabels.size() - 1);
                            }
                        } else {
                            if (pivot.getPosition() == 0) {
                                flip(clickLabels.get(0));
                                pivot.move(-1);
                            } else {
                                flip(clickLabels.get(pivot.getPosition()));
                                pivot.move(pivot.getPosition() - 1);
                            }
                        }
                    } else {
                        if (pivot.getPosition() == -1) {
                            if (clickLabels != null) {
                                pivot.move(clickLabels.size() - 1);
                            }
                        } else {
                            if (pivot.getPosition() == 0)
                                pivot.move(-1);
                            else pivot.move(pivot.getPosition() - 1);
                        }
                    }
                }
            });

            Shortcutter.register(this, "_DOWN", new Runnable() {
                @Override
                public void run() {
                    if (Shortcutter.isShiftPressed) {
                        if (pivot.getPosition() == -1) {
                            if (clickLabels != null)
                                pivot.move(0);
                        } else {
                            if (pivot.getPosition() == clickLabels.size() - 1) {
                                flip(clickLabels.get(clickLabels.size() - 1));
                                pivot.move(-1);
                            } else {
                                flip(clickLabels.get(pivot.getPosition()) );
//                                movePivot(pivot + 1);
                                pivot.move(pivot.getPosition() + 1);
                            }
                        }
                    } else {
                        if (pivot.getPosition() == -1) {
                            if (clickLabels != null)
                                pivot.move(0);
                        } else {
                            if (pivot.getPosition() == clickLabels.size() - 1)
                                pivot.move(-1);
                            else
                                pivot.move(pivot.getPosition() + 1);
                        }
                    }
                }
            });

            Shortcutter.register(this, "_DELETE", new DeleteAction(smallLog).runnable);
            Shortcutter.register(this, "_BACK_SPACE", new DeleteAction(smallLog).runnable);

            Shortcutter.register(this, "META_Z", new Runnable() {
                        @Override
                        public void run() {
                            frameHolder.undo();
                        }
                    });
            Shortcutter.register(this, "SHIFT_BACK_SPACE", new MultipleDeleteAction(smallLog).runnable);
            Shortcutter.register(this, "SHIFT_DELETE", new MultipleDeleteAction(smallLog).runnable);
            Shortcutter.register(this, "SHIFT_E", new ExportAction(smallLog).runnable);
            Shortcutter.register(this, "SHIFT_Q", new CollapseAction(smallLog).runnable);
            Shortcutter.register(this, "SHIFT_A", new SelectAllAction(smallLog).runnable);


        }


        public void update(){
            this.revalidate();
            this.repaint();
        }

        private void setAppearance(Dimension dimension1){
            setForeground(frg);
            setBackground(bck);
            setSize(dimension1);
            setVisible(true);
        }

        public void addToCharBuffer(char ch){
            charBuffer.add(ch);
        }

        public String flushCharBuffer(){

            final int n = charBuffer.size();
            StringBuilder sb = new StringBuilder(n);
            for (int i = 0; i < n; i++) {
                sb.append(charBuffer.poll());
            }
            return sb.toString();
        }


        public String colorizeCommand(String text, String command, String color){
            if (text.contains(command)){
                int offset = text.indexOf(command);
                int length = command.length();
                return text.substring(0, offset) + "<font color=\"" + color + "\">" + command + "</font> " + text.substring(offset + length);

            } else {
                return text;
            }
        }

        public void setClickable(final ClickLabel clickLabel){
            clickLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && !e.isConsumed()) {
                        final String text = clickLabel.getText();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (!clickLabel.isStriked()) {
                                    clickLabel.setText(clickLabel.addHtmlTags("<strike>" + clickLabel.takeHtmltags(text) + "</strike>"));
                                    repaint();
                                    smallLog.repaint();
                                    clickLabel.setStriked(true);
                                }
                            }
                        });
                    } else {
                        //shift
                        if (Shortcutter.isShiftPressed && lastClicked != -1) {
                            int current = clickLabels.indexOf(clickLabel);
                            int last = lastClicked;
                            if (last > current) {
                                for (int i = last - 1; i > current; i--) {
                                    flip(clickLabels.get(i), true);
                                }
                                pivot.move(current);
                            } else if (last < current) {
                                for (int i = last + 1; i < current; i++) {
                                    flip(clickLabels.get(i), true);
                                }
                                pivot.move(current);
                            }
                        } else {
                            flip(clickLabel);
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent mouseEvent) {
//                    System.out.println(Arrays.toString(clickLabel.getParent().getComponents()));
                    for (Component component : clickLabel.getParent().getComponents()) {
                        if (component instanceof ClickLabel && !component.equals(clickLabel)) {
                            Point p = new Point((int) (mouseEvent.getLocationOnScreen().getX() - clickLabel.getParent().getLocationOnScreen().getX()), (int) (mouseEvent.getLocationOnScreen().getY() - clickLabel.getParent().getLocationOnScreen().getY()));
                            Rectangle r = component.getBounds();
                            if (r.contains(p)) {
                                System.out.println(((ClickLabel) component).getText());
                                clickLabel.getParent().add(clickLabel, clickLabels.indexOf(((ClickLabel) component)));

                                //swap clickLabels in the model
                                int i = clickLabels.indexOf(clickLabel);
                                int j = clickLabels.indexOf(((ClickLabel) component));
                                if (j > i) {
                                    clickLabels.add(clickLabels.indexOf(((ClickLabel) component)) + 1, clickLabel);
                                    clickLabels.remove(i);
                                } else {
                                    clickLabels.add(clickLabels.indexOf(((ClickLabel) component)), clickLabel);
                                    clickLabels.remove(i + 1);
                                }

                            }

                        }
                        try {
                            //new ResortAction(smallLog).execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        update();
                    }

                }
            });


        }

        public void setDraggable(final ClickLabel clickLabel){
            clickLabel.addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent mouseEvent) {
//                    System.out.println("===[ClickLabel:" + clickLabel.getText() + "]===(Event" + mouseEvent.toString() + ")");
//                    System.out.println("X: " + mouseEvent.getX());
//                    System.out.println("Y: " + mouseEvent.getY());
//                    System.out.println("---------------");
//                    System.out.println("X (on screen):" + mouseEvent.getXOnScreen());
//                    System.out.println("Y (on screen): " + mouseEvent.getYOnScreen());
//                    System.out.println("==============");

//                    clickLabel.setLocation(new Point(mouseEvent.getX(), mouseEvent.getY()));
                    clickLabel.setBounds((int) (mouseEvent.getXOnScreen() - clickLabel.getParent().getLocationOnScreen().getX()), (int) (mouseEvent.getYOnScreen() - clickLabel.getParent().getLocationOnScreen().getY()), clickLabel.getWidth(), clickLabel.getHeight());
                    clickLabel.invalidate();
                    clickLabel.repaint();
                }

                @Override
                public void mouseMoved(MouseEvent mouseEvent) {
                }


            });
        }


        public void flip(ClickLabel cl){
            if(!cl.isSelected()) {
                cl.setBackground(cl.getSelectedColor());
                repaint();
                smallLog.repaint();
                cl.setSelected(true);
                if (clickLabels != null) {
                    lastClicked = clickLabels.indexOf(cl);
                    pivot.move(lastClicked);
                }
            } else {
                cl.setBackground(bck);
                repaint();
                smallLog.repaint();
                cl.setSelected(false);
//                if (pivot == clickLabels.indexOf(cl)) movePivot(-1);
                if (clickLabels != null) {
                    lastClicked = clickLabels.indexOf(cl);
                    pivot.move(lastClicked);
                }
            }
        }

        public void flip(ClickLabel cl, boolean flip_to_selected){
            if(flip_to_selected) {
                cl.setBackground(cl.getSelectedColor());
                repaint();
                smallLog.repaint();
                cl.setSelected(true);
                if (clickLabels != null) {
                    lastClicked = clickLabels.indexOf(cl);
                    pivot.move(lastClicked);
                }
            } else {
                cl.setBackground(bck);
                repaint();
                smallLog.repaint();
                cl.setSelected(false);
//                if (pivot == clickLabels.indexOf(cl)) movePivot(-1);
                if (clickLabels != null) {
                    lastClicked = clickLabels.indexOf(cl);
                    pivot.move(lastClicked);
                }
            }
        }



        public void putClickLabel(int index, ClickLabel cl){
            clickLabels.add(index, cl);
            semiTransparentPanel.add(cl, index);
        }

        public void deleteClickLabel(ClickLabel cl){
            this.remove(cl);
            clickLabels.remove(cl);
        }

        public void setLastClicked(int lastClicked) {
            this.lastClicked = lastClicked;
        }
    }



}
