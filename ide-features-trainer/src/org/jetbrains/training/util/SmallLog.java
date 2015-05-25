package org.jetbrains.training.util;

import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shortcutter.Shortcutter;
import shortcutter.WrongShortcutException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by karashevich on 14/04/15.
 */
public class SmallLog extends JFrame{

    private static Dimension dimension = new Dimension(300, 600);
    private static Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    private static Color bck = new Color(54, 54 , 55, 255);
    private static Color frg = new Color(230, 230, 230, 230);
    private static Color transparentColor = new Color(0, 0, 0, 0);


    final public static String ACTION = "[Action System]";
    final public static String TYPING = "[Typing]";
    final public static String PROMPT = "> ";


    private SemiTransparentPanel semiTransparentPanel;

    public SmallLog() throws WrongShortcutException {
        super("SmallLog");

        setAlwaysOnTop(true);


        semiTransparentPanel = new SemiTransparentPanel(dimension, this);
        semiTransparentPanel.addLine("Initial message");
        semiTransparentPanel.addLine("Let's start log here!");
        semiTransparentPanel.addLine(ACTION + "Some action here");

        JBScrollPane jbScrollPane = new JBScrollPane(semiTransparentPanel);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        add(jbScrollPane);
        semiTransparentPanel.setVisible(true);
        setSize(dimension);
        setVisible(true);
    }

    public void addLine(String text){
        semiTransparentPanel.addLine(text);
    }

    public void addChar(char c) {
        semiTransparentPanel.addToCharBuffer(c);
    }


    private class SemiTransparentPanel extends JPanel{

        private static final int V_GAP = 2;
        private SmallLog smallLog;
        private int lastClicked = -1;
        private Pivot pivot;

        //clickLabels
        private final ArrayList<ClickLabel> clickLabels = new ArrayList<ClickLabel>();
        private final java.util.Queue<Character> charBuffer = new ArrayBlockingQueue<Character>(1000);

        public SemiTransparentPanel(Dimension dimension1, SmallLog smallLog) throws WrongShortcutException {

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
                        if (cl.selected) flip(cl);
                    }
                    pivot.move(-1);
                }
            });

            Runnable deleteAction = new Runnable() {
                @Override
                public void run() {

                    if (pivot.getPosition() != -1) {
                        if (clickLabels.size() > pivot.getPosition()) {
                            int deleted = pivot.getPosition();
                            deleteClickLabel(clickLabels.get(pivot.getPosition()));
                            //init pivot again
                            if (deleted == clickLabels.size()) pivot.move(clickLabels.size() - 1);
                            else pivot.move(deleted);
                            update();
                        }
                    }
                }
            };

            //delete clickLabel if user pressed DEL ot BACKSPACE
            Shortcutter.register(this, "_DELETE", deleteAction);
            Shortcutter.register(this, "_BACK_SPACE", deleteAction);

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

            Runnable multipleDelete = new Runnable() {
                @Override
                public void run() {

                    //build set of marked clicklabels
                    final ArrayList<ClickLabel> markedClickLabels = new ArrayList<ClickLabel>();
                    for (ClickLabel clickLabel: clickLabels) if(clickLabel.selected) markedClickLabels.add(clickLabel);

                    for (ClickLabel clickLabel: markedClickLabels) {
                        deleteClickLabel(clickLabel);
                    }
                    while(!markedClickLabels.isEmpty()) {
                        markedClickLabels.remove(0);
                    }

                    pivot.move(-1);
                    update();
                }
            };


            Shortcutter.register(this, "SHIFT_BACK_SPACE", multipleDelete);
            Shortcutter.register(this, "SHIFT_DELETE", multipleDelete);

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
                                flip(clickLabels.get(pivot.getPosition()));
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
        }

        private void update(){
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

        public synchronized void addLine(final String line){
            if (!charBuffer.isEmpty()) {
                String cb = flushCharBuffer();
                ClickLabel cLabel1 = new ClickLabel(smallLog, PROMPT + colorizeCommand(TYPING, TYPING, "green") + " " + cb);
                clickLabels.add(cLabel1);
                setClickable(cLabel1);
                this.add(cLabel1);
                final Component rigidArea = Box.createRigidArea(new Dimension(0, V_GAP));
                cLabel1.setVerticalSpace(rigidArea);
                this.add(rigidArea);
                update();
                smallLog.repaint();

            }
            if(line.contains(ACTION)){
                ClickLabel cLabel = new ClickLabel(smallLog, PROMPT + colorizeCommand(line, ACTION, "red"));
                clickLabels.add(cLabel);
                setClickable(cLabel);
                this.add(cLabel);
                final Component rigidArea = Box.createRigidArea(new Dimension(0, V_GAP));
                cLabel.setVerticalSpace(rigidArea);
                this.add(rigidArea);
                this.revalidate();
                this.repaint();
                smallLog.repaint();
            } else {
                ClickLabel cLabel = new ClickLabel(smallLog, PROMPT + line);
                clickLabels.add(cLabel);
                setClickable(cLabel);
                this.add(cLabel);
                final Component rigidArea = Box.createRigidArea(new Dimension(0, V_GAP));
                cLabel.setVerticalSpace(rigidArea);
                this.add(rigidArea);
                this.repaint();
                this.revalidate();
                smallLog.repaint();
            }
        }


        private String colorizeCommand(String text, String command, String color){
            if (text.contains(command)){
                int offset = text.indexOf(command);
                int length = command.length();
                return text.substring(0, offset) + "<font color=\"" + color + "\">" + command + "</font> " + text.substring(offset + length);

            } else {
                return text;
            }
        }

        private void setClickable(final ClickLabel clickLabel){
            clickLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && !e.isConsumed()) {
                        e.consume();
                        final String text = clickLabel.getText();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (!clickLabel.striked){
                                    clickLabel.setText(clickLabel.addHtmlTags("<strike>" + clickLabel.takeHtmltags(text) + "</strike>"));
                                    repaint();
                                    smallLog.repaint();
                                    clickLabel.striked = true;
                                }
                            }
                        });
                    } else {
                        e.consume();
                        if (Shortcutter.isShiftPressed && lastClicked != -1){
                            int current = clickLabels.indexOf(clickLabel);
                            int last = lastClicked;
                            if (last > current) {
                                for(int i = last - 1; i >= current; i-- ){
                                    flip(clickLabels.get(i));
                                }
                            } else if (last < current) {
                                for(int i = last + 1; i <= current; i++ ){
                                    flip(clickLabels.get(i));
                                }
                            }
                        } else {
                            e.consume();
                            flip(clickLabel);
                        }
                    }
                }
            });
        }

        public void flip(ClickLabel cl){
            if(!cl.selected) {
                cl.setBackground(cl.selectedColor);
                repaint();
                smallLog.repaint();
                cl.selected = true;
                if (clickLabels != null) {
                    lastClicked = clickLabels.indexOf(cl);
                    pivot.move(lastClicked);
                }
            } else {
                cl.setBackground(bck);
                repaint();
                smallLog.repaint();
                cl.selected = false;
//                if (pivot == clickLabels.indexOf(cl)) movePivot(-1);
                if (clickLabels != null) {
                    lastClicked = clickLabels.indexOf(cl);
                    pivot.move(lastClicked);
                }
            }
        }

        public void deleteClickLabel(ClickLabel cl){
            if (cl.getVerticalSpace() != null)
                this.remove(cl.getVerticalSpace());
            this.remove(cl);
            clickLabels.remove(cl);
        }

    }


    private class ClickLabel extends JLabel{

        private boolean striked;
        private boolean selected;
        private final Color selectedColor = new Color(34, 132, 255, 255);
        private final Color pivotColor = new Color(104, 196, 255, 255);
        private Component verticalSpace = null;

        private SmallLog smallLog;

        public ClickLabel(final SmallLog smallLog, String text){
            setFont(font);
            setForeground(Color.white);

            this.setText(addHtmlTags(text));
            this.smallLog = smallLog;
            striked = false;
            selected = false;

            setOpaque(true);
            setVisible(true);
            setBackground(bck);
        }

        public Component getVerticalSpace() {
            return verticalSpace;
        }

        public void setVerticalSpace(Component verticalSpace) {
            this.verticalSpace = verticalSpace;
        }

        public String takeHtmltags(String in){
            if(in.length() > 12 && in.substring(0,6).equals("<html>") && in.substring(in.length() - 7).equals("</html>"))
                return in.substring(6, in.length() - 7);
            else return in;
        }

        public String addHtmlTags(String in){
            return ("<html>" + in + "</html>");
        }

    }

    private  class Pivot {
        private ArrayList<ClickLabel> clickLabels;
        private ClickLabel pivotClickLabel;
        private int p;

        public Pivot(int p, @Nullable ClickLabel pivotClickLabel, @NotNull ArrayList<ClickLabel> clickLabels) {
            this.clickLabels = clickLabels;
            this.p = p;
            this.pivotClickLabel = pivotClickLabel;
        }

        public int getPosition(){
            if (clickLabels.contains(pivotClickLabel)) {
                p = clickLabels.indexOf(pivotClickLabel);
                return p;
            } else {
                return -1;
            }
        }

        public void move(int position){
            //check current position
            if (pivotClickLabel !=null && clickLabels.contains(pivotClickLabel)) {
                unmark(pivotClickLabel);
                if (position >= clickLabels.size() || position < 0) {
                    pivotClickLabel = null;
                    p = -1;
                } else {
                    p = position;
                    pivotClickLabel = clickLabels.get(p);
                    mark(pivotClickLabel);
                }
            } else {
                if (position >= clickLabels.size() || position < 0) {
                    pivotClickLabel = null;
                    p = -1;
                } else {
                    p = position;
                    pivotClickLabel = clickLabels.get(p);
                    mark(pivotClickLabel);
                }
            }
        }

        public void move_and_select(int position){
            if (position < clickLabels.size() && position >= 0) {
                move(position);
                clickLabels.get(position).selected = true;
            }
        }

        private void mark(ClickLabel clickLabel){
            clickLabel.setBackground(clickLabel.pivotColor);
        }

        private void unmark(ClickLabel clickLabel){
            if (clickLabel.selected)
                clickLabel.setBackground(clickLabel.selectedColor);
            else
                clickLabel.setBackground(bck);
        }
    }
}
