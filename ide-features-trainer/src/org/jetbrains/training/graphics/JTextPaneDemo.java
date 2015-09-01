package org.jetbrains.training.graphics;

/**
 * Created by Sergey.Karashevich on 9/1/2015.
 */
import org.jetbrains.training.util.MyClassLoader;

import java.awt.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class JTextPaneDemo extends JFrame {

    JTextPane textPane = new JTextPane();

    static SimpleAttributeSet REGULAR = new SimpleAttributeSet();
    static SimpleAttributeSet BOLD = new SimpleAttributeSet();
    static SimpleAttributeSet ROBOTO = new SimpleAttributeSet();
    static SimpleAttributeSet CODE = new SimpleAttributeSet();

    static SimpleAttributeSet PARAGRAPH_STYLE = new SimpleAttributeSet();

    static {
        initStyleConstants();
    }

    private static void initStyleConstants() {
        StyleConstants.setFontFamily(REGULAR, "Dialog");
        StyleConstants.setFontSize(REGULAR, 12);
        StyleConstants.setForeground(REGULAR, Color.BLACK);

        StyleConstants.setFontFamily(BOLD, "Dialog");
        StyleConstants.setFontSize(BOLD, 12);
        StyleConstants.setBold(BOLD, true);
        StyleConstants.setForeground(BOLD, Color.BLACK);
        StyleConstants.setLineSpacing(REGULAR, 16);


        StyleConstants.setForeground(CODE, Color.BLUE);
        StyleConstants.setFontFamily(CODE, "Monospaced");
        StyleConstants.setFontSize(CODE, 12);

        StyleConstants.setLeftIndent(PARAGRAPH_STYLE, 0);
        StyleConstants.setRightIndent(PARAGRAPH_STYLE, 0);
        StyleConstants.setSpaceAbove(PARAGRAPH_STYLE, 10.5f);
        StyleConstants.setSpaceBelow(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setLineSpacing(PARAGRAPH_STYLE, 0.0f);
    }

    public JTextPaneDemo() {
        super("JTextPane Demo");



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



        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.PAGE_AXIS));

        textPane.setEditable(false);

        textPane.setParagraphAttributes(PARAGRAPH_STYLE, true);
        insertText("\n<html>Put cursor before any word. Select it with </html>", REGULAR);
        insertText("Meta + Up", BOLD);
        setEndSelection();
        insertText("\nNow let's select the whole line with ", REGULAR);
        insertText("Meta + Alt + Up", BOLD);
        setEndSelection();

        setSize(500, 450);
        jPanel.add(textPane);

        add(jPanel);
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