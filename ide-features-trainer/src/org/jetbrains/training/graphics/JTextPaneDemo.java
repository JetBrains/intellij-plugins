package org.jetbrains.training.graphics;

/**
 * Created by Sergey.Karashevich on 9/1/2015.
 */
import org.jetbrains.training.util.MyClassLoader;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class JTextPaneDemo extends JFrame {
    static SimpleAttributeSet ITALIC_GRAY = new SimpleAttributeSet();

    static SimpleAttributeSet BOLD_BLACK = new SimpleAttributeSet();

    static SimpleAttributeSet BLACK = new SimpleAttributeSet();

    static SimpleAttributeSet ROBOTO = new SimpleAttributeSet();

    static SimpleAttributeSet CODE = new SimpleAttributeSet();

    JTextPane textPane = new JTextPane();

    // Best to reuse attribute sets as much as possible.
    static {
        StyleConstants.setForeground(ITALIC_GRAY, Color.gray);
        StyleConstants.setItalic(ITALIC_GRAY, true);
        StyleConstants.setFontFamily(ITALIC_GRAY, "Helvetica");
        StyleConstants.setFontSize(ITALIC_GRAY, 14);

        StyleConstants.setForeground(CODE, Color.BLUE);
        StyleConstants.setFontFamily(CODE, "Monospaced");
        StyleConstants.setFontSize(CODE, 14);


        StyleConstants.setForeground(BOLD_BLACK, Color.black);
        StyleConstants.setBold(BOLD_BLACK, true);
        StyleConstants.setFontFamily(BOLD_BLACK, "Helvetica");
        StyleConstants.setFontSize(BOLD_BLACK, 14);

        StyleConstants.setForeground(BLACK, Color.black);
        StyleConstants.setFontFamily(BLACK, "Helvetica");
        StyleConstants.setFontSize(BLACK, 14);


        final String customFontPath = "roboto.ttf";
        Font customFont = null;
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, MyClassLoader.getInstance().getResourceAsStream(customFontPath));
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        StyleConstants.setForeground(ROBOTO, Color.black);
        StyleConstants.setFontFamily(ROBOTO, customFont.getFamily());
        StyleConstants.setFontSize(ROBOTO, 14);


    }

    public JTextPaneDemo() {
        super("JTextPane Demo");
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.black, Color.GRAY));
        JLabel jLabel = new JLabel("ctrl + up");
        jLabel.setBackground(Color.PINK);
        jLabel.setBorder(new LineBorder(Color.RED,1, true));
        panel.add(jLabel, BorderLayout.CENTER);

        textPane.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textPane);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        setEndSelection();
        textPane.insertIcon(new ImageIcon("java2sLogo.GIF"));
        insertText("\nWebsite for: www.java2s.com \n\n", BOLD_BLACK);

        setEndSelection();
        insertText("                                    ", BLACK);
        setEndSelection();
        insertText("\n Roboto font test Roboto font test Roboto font test Roboto font test Roboto font test", ROBOTO);
        insertText("\uF0FC", ROBOTO);
        setEndSelection();
        insertText("\nSome code here", CODE);
        setEndSelection();
        insertText("\n      Java            "
                        + "                                    " + "Source\n\n",
                ITALIC_GRAY);

        insertText(" and Support. \n", BLACK);
        textPane.insertComponent(jLabel);


        setEndSelection();


        setSize(500, 450);
        setVisible(true);
    }

    protected void insertText(String text, AttributeSet set) {
        try {
            textPane.getDocument().insertString(
                    textPane.getDocument().getLength(), text, set);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // Needed for inserting icons in the right places
    protected void setEndSelection() {
        textPane.setSelectionStart(textPane.getDocument().getLength());
        textPane.setSelectionEnd(textPane.getDocument().getLength());
    }

    public static void main(String argv[]) {
        new JTextPaneDemo();
    }
}