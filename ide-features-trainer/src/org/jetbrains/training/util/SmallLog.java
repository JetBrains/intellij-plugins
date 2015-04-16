package org.jetbrains.training.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    public SmallLog(){
        super("SmallLog");

        setAlwaysOnTop(true);

        semiTransparentPanel = new SemiTransparentPanel(dimension, this);
        semiTransparentPanel.addLine("Initial message");
        semiTransparentPanel.addLine("Let's start log here!");
        semiTransparentPanel.addLine(ACTION + "Some action here");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        add(semiTransparentPanel);
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

        //clickLabels
        private final ArrayList<ClickLabel> clickLabels = new ArrayList<ClickLabel>();
        private final java.util.Queue<Character> charBuffer = new ArrayBlockingQueue<Character>(1000);

        public SemiTransparentPanel(Dimension dimension1, SmallLog smallLog){
            this.smallLog = smallLog;
            setSize(dimension1);
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setAppearance(dimension1);
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
                this.add(cLabel1);
                this.add(Box.createRigidArea(new Dimension(0, V_GAP)));
                this.repaint();
                smallLog.repaint();
            }
            if(line.contains(ACTION)){
                ClickLabel cLabel = new ClickLabel(smallLog, PROMPT + colorizeCommand(line, ACTION, "red"));
                clickLabels.add(cLabel);
                this.add(cLabel);
                this.add(Box.createRigidArea(new Dimension(0, V_GAP)));
                this.repaint();
                smallLog.repaint();
            } else {
                ClickLabel cLabel = new ClickLabel(smallLog, PROMPT + line);
                clickLabels.add(cLabel);
                this.add(cLabel);
                this.add(Box.createRigidArea(new Dimension(0, V_GAP)));
                this.repaint();
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

    }


    private class ClickLabel extends JLabel{

        private boolean striked;
        private boolean selected;

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


            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && !e.isConsumed()) {
                        e.consume();
                        final String text = getText();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if(!striked) {
                                    setText(addHtmlTags("<strike>" + takeHtmltags(text) + "</strike>"));
                                    repaint();
                                    smallLog.repaint();
                                    striked = true;
                                }
                            }
                        });
                    } else {
                        e.consume();
                        if(!selected) {
                            setBackground(new Color(118, 139, 198, 255));
                            repaint();
                            smallLog.repaint();
                            selected = true;
                        } else {
                            setBackground(bck);
                            repaint();
                            smallLog.repaint();
                            selected = false;
                        }
                    }
                }
            });
        }

        private String takeHtmltags(String in){
            if(in.length() > 12 && in.substring(0,6).equals("<html>") && in.substring(in.length() - 7).equals("</html>"))
                return in.substring(6, in.length() - 7);
            else return in;
        }

        private String addHtmlTags(String in){
            return ("<html>" + in + "</html>");
        }

    }
}
