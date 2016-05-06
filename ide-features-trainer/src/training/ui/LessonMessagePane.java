package training.ui;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import icons.LearnIcons;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

/**
 * Created by karashevich on 01/09/15.
 */

class LessonMessagePane extends JTextPane {

    //Style Attributes for LessonMessagePane(JTextPane)
    private static SimpleAttributeSet REGULAR = new SimpleAttributeSet();
    private static SimpleAttributeSet BOLD = new SimpleAttributeSet();
    private static SimpleAttributeSet SHORTCUT = new SimpleAttributeSet();
    private static SimpleAttributeSet ROBOTO = new SimpleAttributeSet();
    private static SimpleAttributeSet CODE = new SimpleAttributeSet();
    private static SimpleAttributeSet LINK = new SimpleAttributeSet();

    private static SimpleAttributeSet PARAGRAPH_STYLE = new SimpleAttributeSet();

    private ArrayList<LessonMessage> lessonMessages = new ArrayList<>();
    private Color passedColor = JBColor.GRAY;
    private static int arc = 4;
    private static int indent = 2;
//    private JBColor bckshrtct = new JBColor(new Color(218, 226, 237), new Color(39, 43, 46));
    private JBColor bckShortcutColor;
    private Icon passedIcon;
    private int fontSize;

    LessonMessagePane(int fontSize, int leftIndent) {
        super();
        this.fontSize = fontSize;
        initStyleConstants(leftIndent);
        setEditable(false);
        this.setParagraphAttributes(PARAGRAPH_STYLE, true);
    }

    private void initStyleConstants(int leftIndent) {
        StyleConstants.setFontFamily(REGULAR, UIUtil.getLabelFont().getFamily());
        StyleConstants.setFontSize(REGULAR, fontSize);
        StyleConstants.setForeground(REGULAR, JBColor.BLACK);

        StyleConstants.setFontFamily(BOLD, UIUtil.getLabelFont().getFamily());
        StyleConstants.setFontSize(BOLD, fontSize);
        StyleConstants.setBold(BOLD, true);
        StyleConstants.setForeground(BOLD, JBColor.BLACK);

        StyleConstants.setFontFamily(SHORTCUT, UIUtil.getLabelFont().getFamily());
        StyleConstants.setFontSize(SHORTCUT, fontSize);
        StyleConstants.setBold(SHORTCUT, true);
        StyleConstants.setForeground(SHORTCUT, JBColor.BLACK);

        StyleConstants.setForeground(CODE, JBColor.BLUE);
        StyleConstants.setFontFamily(CODE, EditorColorsManager.getInstance().getGlobalScheme().getEditorFontName());
        StyleConstants.setFontSize(CODE, fontSize);

        StyleConstants.setForeground(LINK, JBColor.BLUE);
        StyleConstants.setFontFamily(LINK, UIUtil.getLabelFont().getFamily());
        StyleConstants.setUnderline(LINK, true);
        StyleConstants.setFontSize(LINK, fontSize);

        StyleConstants.setLeftIndent(PARAGRAPH_STYLE, leftIndent);
        StyleConstants.setRightIndent(PARAGRAPH_STYLE, 0);
        StyleConstants.setSpaceAbove(PARAGRAPH_STYLE, 16.0f);
        StyleConstants.setSpaceBelow(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setLineSpacing(PARAGRAPH_STYLE, 0.2f);
    }

    void setUI(Color regularFontColor,
               Color shortcutColor,
               Color codeFontColor,
               Color linkFontColor,
               Color passedColor,
               JBColor shortcutBackgroundColor) {
        StyleConstants.setForeground(REGULAR, regularFontColor);
        StyleConstants.setForeground(BOLD, shortcutColor);
        StyleConstants.setForeground(SHORTCUT, shortcutColor);
        StyleConstants.setForeground(LINK, linkFontColor);
        StyleConstants.setForeground(CODE, codeFontColor);
        this.passedColor = passedColor;
        this.bckShortcutColor = shortcutBackgroundColor;
    }

    void addMessage(String text) {
        try {
            int start = getDocument().getLength();
            getDocument().insertString(getDocument().getLength(), text, REGULAR);
            int end = getDocument().getLength();
            lessonMessages.add(new LessonMessage(text, start, end));

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    void addMessage(Message[] messages) {

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
                final int startOffset = getDocument().getEndPosition().getOffset();
                message.setStartOffset(startOffset);
                final String text = message.getText();
                switch (message.getType()) {
                    case TEXT_REGULAR:
                        getDocument().insertString(getDocument().getLength(), text, REGULAR);
                        break;

                    case TEXT_BOLD:
                        getDocument().insertString(getDocument().getLength(), text, BOLD);
                        break;

                    case SHORTCUT:
                        //add extra spaces at the start and in the end of text
                        getDocument().insertString(getDocument().getLength(), " " + text + " ", SHORTCUT);
                        break;

                    case CODE:
                        getDocument().insertString(getDocument().getLength(), text, CODE);
                        break;

                    case CHECK:
                        getDocument().insertString(getDocument().getLength(), text, ROBOTO);
                        break;

                    case LINK:
                        appendLink(message);
                        break;

                }
                final int endOffset = getDocument().getEndPosition().getOffset();
                message.setEndOffset(endOffset);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        int end = getDocument().getLength();
        lessonMessages.add(new LessonMessage(messages, start, end));
    }

    /**
     * inserts a checkmark icon to the end of the LessonMessagePane document as a styled label.
     */
    void passPreviousMessages() throws BadLocationException {
        if (lessonMessages.size() > 0) {
            final LessonMessage lessonMessage = lessonMessages.get(lessonMessages.size() - 1);
            lessonMessage.setPassed(true);

            //Repaint text with passed style
            Style passedStyle = this.addStyle("PassedStyle", null);
            StyleConstants.setForeground(passedStyle, passedColor);
            final StyledDocument doc = getStyledDocument();

            doc.setCharacterAttributes(0, lessonMessage.getEnd(), passedStyle, false);
        }
    }

    public void clear() {
        setText("");
        lessonMessages.clear();
    }

    /**
     * Appends link inside JTextPane to Run another lesson
     *
     * @param message - should have LINK type. message.runnable starts when the message has been clicked.
     */
    private void appendLink(final Message message) throws BadLocationException {
        final int startLink = getDocument().getEndPosition().getOffset();
        getDocument().insertString(getDocument().getLength(), message.getText(), LINK);
        final int endLink = getDocument().getEndPosition().getOffset();

        addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent me) {
                int x = me.getX();
                int y = me.getY();

                int clickOffset = viewToModel(new Point(x, y));
                if (startLink <= clickOffset && clickOffset <= endLink && message.getRunnable() != null) {
                    message.getRunnable().run();
                }

            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        try {
            paintShortcutBackground(g);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        super.paintComponent(g);
        paintLessonCheckmarks(g);
    }

    private void paintLessonCheckmarks(Graphics g) {
        for (LessonMessage lessonMessage : lessonMessages) {
            if (lessonMessage.isPassed()) {
                int startOffset = lessonMessage.getStart();
                if (startOffset != 0) startOffset++;
                try {
                    Rectangle rectangle = modelToView(startOffset);
                    LearnIcons.CheckmarkGray12.paintIcon(this, g, rectangle.x - 17, rectangle.y + 1);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void paintShortcutBackground(Graphics g) throws BadLocationException {
        Graphics2D g2d = (Graphics2D) g;
        for (LessonMessage lessonMessage : lessonMessages) {
            final ArrayList<Message> myMessages = lessonMessage.getMyMessages();
            for (Message myMessage : myMessages) {
                if (myMessage.getType().equals(Message.MessageType.SHORTCUT)) {
                    final int startOffset = myMessage.getStartOffset();
                    final int endOffset = myMessage.getEndOffset();
                    final Rectangle rectangleStart = modelToView(startOffset);
                    final Rectangle rectangleEnd = modelToView(endOffset - 2);
                    final Color color = g2d.getColor();
                    g2d.setColor(bckShortcutColor);
                    RoundRectangle2D r2d = new RoundRectangle2D.Double(rectangleStart.getX() - 2 * indent, rectangleStart.getY() - indent,
                            (rectangleEnd.getX() - rectangleStart.getX()) + 4 * indent, fontSize + 3 * indent, arc, arc);
                    g2d.fill(r2d);
                    g2d.setColor(color);
                }
            }
        }
    }

    public AttributeSet getDefaultAttributeSet() {
        return REGULAR;
    }


}
