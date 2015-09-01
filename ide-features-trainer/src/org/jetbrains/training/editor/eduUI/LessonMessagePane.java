package org.jetbrains.training.editor.eduUI;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by karashevich on 01/09/15.
 */

public class LessonMessagePane extends JTextPane {

    //Style Attributes for LessonMessagePane(JTextPane)
    static SimpleAttributeSet REGULAR = new SimpleAttributeSet();
    static SimpleAttributeSet BOLD = new SimpleAttributeSet();
    static SimpleAttributeSet ROBOTO = new SimpleAttributeSet();
    static SimpleAttributeSet CODE = new SimpleAttributeSet();

    static SimpleAttributeSet PARAGRAPH_STYLE = new SimpleAttributeSet();

    private ArrayList<LessonMessage> lessonMessages = new ArrayList<LessonMessage>();

    LessonMessagePane(){
        super();
        initStyleConstants();
        setEditable(false);
        this.setParagraphAttributes(PARAGRAPH_STYLE, true);
    }

    private static void initStyleConstants() {
        //TODO: Change FamilyFont to UiUtil fonts
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


    public void addMessage(String text) {
        try {
            int start = getDocument().getLength();
            getDocument().insertString(getDocument().getLength(), text, REGULAR);
            int end = getDocument().getLength();
            lessonMessages.add(new LessonMessage(text, start, end));

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void addMessage(Message[] messages) {

        int start = getDocument().getLength();
        if (lessonMessages.size() > 0) {
            try {
                getDocument().insertString(getDocument().getLength(), "\n", REGULAR);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        for (Message message : messages) {
            try {
                switch(message.getType()){
                    case TEXT_REGULAR:
                        getDocument().insertString(getDocument().getLength(), message.getText(), REGULAR);
                        break;

                    case SHORTCUT:
                        getDocument().insertString(getDocument().getLength(), message.getText(), BOLD);
                        break;

                    case CODE:
                        getDocument().insertString(getDocument().getLength(), message.getText(), CODE);
                        break;

                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        int end = getDocument().getLength();
        lessonMessages.add(new LessonMessage(messages, start, end));
    }

    public void passPreviousMessages() {
        Style passedStyle = this.addStyle("PassedStyle", null);
        StyleConstants.setForeground(passedStyle, Color.GRAY);
        final StyledDocument doc = getStyledDocument();
        if (lessonMessages.size() > 0) {
            doc.setCharacterAttributes(0, lessonMessages.get(lessonMessages.size() - 1).getEnd(), passedStyle, false);
        }
    }

    public void clear() {
        setText("");
        lessonMessages.clear();
    }
}
