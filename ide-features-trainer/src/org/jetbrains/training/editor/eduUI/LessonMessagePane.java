package org.jetbrains.training.editor.eduUI;

import com.intellij.execution.process.OSProcessManager;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.ui.UIUtil;

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
    private Color passedColor = Color.GRAY;
    private Icon passedIcon;

    LessonMessagePane(){
        super();
        initStyleConstants();
        setEditable(false);
        this.setParagraphAttributes(PARAGRAPH_STYLE, true);

        passedIcon = IconLoader.findIcon(EduIcons.CHECKMARK_GRAY_12);

    }

    private static void initStyleConstants() {
        StyleConstants.setFontFamily(REGULAR, UIUtil.getLabelFont().getFamily());
        StyleConstants.setFontSize(REGULAR, 12);
        StyleConstants.setForeground(REGULAR, Color.BLACK);

        StyleConstants.setFontFamily(BOLD, UIUtil.getLabelFont().getFamily());
        StyleConstants.setFontSize(BOLD, 12);
        StyleConstants.setBold(BOLD, true);
        StyleConstants.setForeground(BOLD, Color.BLACK);

        StyleConstants.setForeground(CODE, Color.BLUE);
        StyleConstants.setFontFamily(CODE, EditorColorsManager.getInstance().getGlobalScheme().getEditorFontName());
        StyleConstants.setFontSize(CODE, 12);

        StyleConstants.setLeftIndent(PARAGRAPH_STYLE, 0);
        StyleConstants.setRightIndent(PARAGRAPH_STYLE, 0);
        StyleConstants.setSpaceAbove(PARAGRAPH_STYLE, 10.5f);
        StyleConstants.setSpaceBelow(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setLineSpacing(PARAGRAPH_STYLE, 0.0f);
    }

    public void setUI(Color regularFontColor,
                      Color shortcutColor,
                      Color codeFontColor,
                      Color passedColor){
        StyleConstants.setForeground(REGULAR, regularFontColor);
        StyleConstants.setForeground(BOLD, shortcutColor);
        StyleConstants.setForeground(CODE, codeFontColor);
        this.passedColor = passedColor;

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

                    case CHECK:
                        getDocument().insertString(getDocument().getLength(), message.getText(), ROBOTO);
                        break;

                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        int end = getDocument().getLength();
        lessonMessages.add(new LessonMessage(messages, start, end));
    }

    public void passPreviousMessages() throws BadLocationException {
        if (lessonMessages.size() > 0) {
            final LessonMessage lessonMessage = lessonMessages.get(lessonMessages.size() - 1);
            if(SystemInfo.isMac) {
                lessonMessage.appendMacCheck();
                getDocument().insertString(getDocument().getLength(), " ", REGULAR);
                insertIcon(passedIcon);
            } else {
                lessonMessage.appendWinCheck();
                getDocument().insertString(getDocument().getLength(), " \uF0FC", ROBOTO);
            }

            Style passedStyle = this.addStyle("PassedStyle", null);
            StyleConstants.setForeground(passedStyle, passedColor);
            final StyledDocument doc = getStyledDocument();

            doc.setCharacterAttributes(0, lessonMessage.getEnd(), passedStyle, false);
        } else return;
    }

    public void clear() {
        setText("");
        lessonMessages.clear();
    }

    public AttributeSet getDefaultAttributeSet() {
        return REGULAR;
    }
}
