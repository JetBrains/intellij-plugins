package training.util;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by karashevich on 14/04/15.
 */
public class SmallLogEx extends JFrame{

    private static Dimension dimension = new Dimension(300, 600);
    private static Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    private static Color bck = new Color(0, 0 ,0, 160);
    private static Color frg = new Color(230, 230, 230, 230);
    private static Color transparentColor = new Color(0, 0, 0, 0);

    final public static String ACTION_SYSTEM = "[Action System]";
    final public static String TYPING = "[Typing]";

    private SemiTransparentPanel semiTransparentPanel;

    public SmallLogEx(){
        super("SmallLog");
        setAlwaysOnTop(true);

        semiTransparentPanel = new SemiTransparentPanel(dimension, this);
        semiTransparentPanel.addLine("Initial message");
        semiTransparentPanel.addLine("Let's start log here!");
        semiTransparentPanel.addLine(ACTION_SYSTEM + " Some action triggered.");
        semiTransparentPanel.setVisible(true);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        add(semiTransparentPanel);
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

        private JLabel label;
        final private Boolean addLock = false;
        private SmallLogEx smallLog;
        private final java.util.Queue<String> messages = new ConcurrentLinkedQueue<String>();
        private final java.util.Queue<Character> charBuffer = new ArrayBlockingQueue<Character>(1000);

        public SemiTransparentPanel(Dimension dimension1, SmallLogEx smallLog){
            this.smallLog = smallLog;
            label = new JLabel();
            add(label);
            setSize(dimension1);
            setLayout(new FlowLayout(FlowLayout.LEFT));
            setAppearance(dimension1);
            setLabelAppearance(dimension1);
        }

        private void setLabelAppearance(Dimension dimension1){
            label.setForeground(frg);
            label.setBackground(new Color(210, 210, 0, 0));

            label.setFont(font);
            label.setAlignmentX(LEFT_ALIGNMENT);
            label.setSize(dimension1);
            label.setVisible(true);
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
            if (line.contains(ACTION_SYSTEM) && !charBuffer.isEmpty()){
                messages.add(TYPING + flushCharBuffer());
            }
            messages.add(line);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    synchronized (messages) {
                        if (messages.size() != 0) {
                            String result = takeHtmltags(label.getText());
                            if (messages.size() != 0) {
                                while (!messages.isEmpty()) {
                                    if (messages.peek().contains(ACTION_SYSTEM)) {
                                        result += "> " + colorizeCommand(messages.poll(), ACTION_SYSTEM, "red") + "<br>";
                                    } else if (messages.peek().contains(TYPING)) {
                                            result += "> " + colorizeCommand(messages.poll(), TYPING, "green") +  "<br>";
                                    } else {
                                        result += "> " + messages.poll() + "<br>";
                                              }
                                }
                            }
                            label.setText(addHtmlTags(result));
                            smallLog.repaint();
                        }
                    }
                }
            });
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
