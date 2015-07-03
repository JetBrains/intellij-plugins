package org.jetbrains.training.eduUI.panel;

import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by karashevich on 26/06/15.
 */
public class EduPanel extends JPanel {

    private int width;

    //Lesson panel stuff
    private JPanel lessonPanel;
    private JLabel lessonNameLabel; //Name of the current lesson
    private JPanel lessonMessageContainer; //Contains lesson's messages
    private JButton lessonNextButton;
    private ArrayList<LessonMessage> messages;


    //UI Preferences
    private Color background;
    private int insets;

    //lessonPanel UI
    private int fontSize;
    private int lessonNameGap;
    private int lessonNextButtonGap;
    private int messageGap;
    private Font messageFont;
    private Font lessonNameFont;
    private Color shortcutTextColor;
    private Color shortcutBckColor;
    private Color shortcutBorderColor;
    private Font shortcutFont;
    private Color passedColor; //TODO: should be replaced with UI scheme color

    //separator UI
    private Color separatorColor;
    private int separatorGap;

    //Course panel stuff
    private JPanel coursePanel;

    //coursePanel UI
    private int lessonGap;
    private Color lessonActiveColor;
    private Color lessonInactiveColor;
    private Font lessonsFont;
    private Font allLessonsFont;

    public EduPanel(int width){
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.width = width;

        //Obligatory block
        generalizeUI();
        this.setBackground(background);
        initLessonPanel();
        initCoursePanel();

        lessonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(lessonPanel);
        final Box.Filler filler = new Box.Filler(
                new Dimension(5, separatorGap),    //minimum size of filler
                new Dimension(5, separatorGap),    //preferable size of filler
                new Dimension(5, Short.MAX_VALUE)     //maximum size of filler
        );
        filler.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(filler);
        coursePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(coursePanel);

        //set EduPanel UI
        this.setPreferredSize(new Dimension(width, 100));
        this.setBorder(new EmptyBorder(insets, insets, insets, insets));


        //message test
        addMessage("<html>Comment out any line 23u5 35" +
                "23 45" +
                "23 4" +
                " 234" +
                "2346 34 623462346236 346234 6 2346with <b>Ctrl + /</b> </html>");
        paintPreviousMessagesToGray();
        addMessage("<html>Comment the line with</html>");
        paintPreviousMessagesToGray();
        addMessage("<html>Comment the line with the same shortcut</html>");
        paintPreviousMessagesToGray();
        addMessage("<html>Comment the line with the same shortcut</html>");
        addMessage("<html>Comment the line with the same shortcut</html>");


    }

    private void generalizeUI(){
        //generalize fonts, colors and sizes
        //TODO: change size to UiUtil size
        fontSize = 12;
        insets = 10;
        lessonNameGap = 10;
        lessonNextButtonGap = 10;
        messageGap = 10;
        background = new Color(250,250,250);
        lessonNameFont = new Font(UIUtil.getLabelFont().getName(), Font.BOLD, fontSize);
        messageFont = new Font(UIUtil.getLabelFont().getName(), 0, fontSize);
        passedColor = new Color(105, 105, 105);

        //shortcut UI
        shortcutFont = new Font(UIUtil.getLabelFont().getName(), Font.BOLD, 10);
        shortcutBckColor = Color.WHITE;
        shortcutBorderColor = Color.BLACK;
        shortcutTextColor = Color.BLACK;

        //separator UI
        separatorColor = new Color(222, 222, 222);
        separatorGap = 10;

        //course UI
        lessonGap = 10;
        lessonActiveColor = new Color(167, 167, 167);
        lessonInactiveColor = new Color(17, 96, 166);
        lessonsFont = new Font(UIUtil.getLabelFont().getName(), 0, fontSize);
        allLessonsFont = new Font(UIUtil.getLabelFont().getName(), 0, fontSize - 1);

    }

    private void initLessonPanel(){
        lessonPanel = new JPanel();
        lessonPanel.setLayout(new BoxLayout(lessonPanel, BoxLayout.Y_AXIS));
        lessonPanel.setBackground(background);

        lessonNameLabel = new JLabel("Comment");
        lessonNameLabel.setFont(lessonNameFont);
        lessonNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        lessonMessageContainer = new JPanel();
        messages = new ArrayList<LessonMessage>();
        lessonMessageContainer.setLayout(new BoxLayout(lessonMessageContainer, BoxLayout.Y_AXIS));
        lessonMessageContainer.setAlignmentX(Component.LEFT_ALIGNMENT);


        //Set label UI
        lessonNextButton = new JButton("Next");
        lessonNextButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        lessonNextButton.setVisible(false);

        lessonPanel.add(lessonNameLabel);
        lessonPanel.add(Box.createRigidArea(new Dimension(0, lessonNameGap)));
        lessonPanel.add(lessonMessageContainer);
        lessonPanel.add(Box.createRigidArea(new Dimension(0, lessonNextButtonGap)));
        lessonPanel.add(lessonNextButton);
    }

    public void addMessage(String text){
        JLabel newLabel = new JLabel(text);
        newLabel.setFont(messageFont);
        final LessonMessage lessonMessage = new LessonMessage(newLabel, null);
        messages.add(lessonMessage);
        final JPanel lessonMessagePanel = lessonMessage.getPanel();
        lessonMessagePanel.setBackground(background);
        lessonMessagePanel.setBorder(new EmptyBorder(0, 0, messageGap, 0));

        lessonMessageContainer.add(lessonMessagePanel);

        //Pack lesson panel
        lessonPanel.revalidate();
        this.revalidate();
        this.repaint();
    }

    public void addMessage(String text, String shortcut){
        JLabel newLabel = new JLabel(text);
        newLabel.setFont(messageFont);
        final LessonMessage lessonMessage = new LessonMessage(newLabel, new ShortcutLabel(shortcut,shortcutFont, shortcutTextColor, shortcutBckColor, shortcutBorderColor));
        messages.add(lessonMessage);
        final JPanel lessonMessagePanel = lessonMessage.getPanel();
        lessonMessagePanel.setBackground(background);
        lessonMessagePanel.setBorder(new EmptyBorder(0, 0, messageGap, 0));

        lessonMessageContainer.add(lessonMessagePanel);
        this.revalidate();
        this.repaint();
    }

    private void paintPreviousMessagesToGray(){
        if (messages == null) return;
        for (LessonMessage message : messages) {
//            message.setForeground(passedColor);
            if (message.getLabel().getForeground() != passedColor) {
                message.getLabel().setForeground(passedColor);
                String text = message.getLabel().getText();
                text += " ✔";
                message.getLabel().setText(text);
            }
        }
    }

    private void initCoursePanel(){
        coursePanel = new JPanel();
        coursePanel.setLayout(new BoxLayout(coursePanel, BoxLayout.Y_AXIS));
        coursePanel.setBackground(background);

        //define separator
        coursePanel.setBorder(new MatteBorder(1, 0, 0, 0, separatorColor));
        JLabel allLessons = new JLabel();
        allLessons.setText("ALL LESSONS");
        allLessons.setFont(allLessonsFont);

        coursePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        coursePanel.add(allLessons);
        coursePanel.add(Box.createRigidArea(new Dimension(0, 10)));

        ArrayList<JLabel> lessons = new ArrayList<JLabel>(5);
        for (int i = 0; i < 7; i++) {
            final JLabel e = new JLabel(("Lesson" + i).toString());
            e.setForeground(lessonInactiveColor);
            e.setBorder(new EmptyBorder(0, 0, lessonGap, 0));
            e.setFont(lessonsFont);
            lessons.add(e);
            coursePanel.add(e);
        }

        lessons.get(0).setText("Delete");
        lessons.get(1).setText("Duplicate");
        lessons.get(2).setText("Comment");
        lessons.get(2).setForeground(lessonActiveColor);
        lessons.get(3).setText("Surround with ...");
        lessons.get(4).setText("Select code blocks");
        lessons.get(5).setText("Smart split and join");
        lessons.get(6).setText("Toggle case");
        coursePanel.setMaximumSize(new Dimension(width - insets * 2, coursePanel.getPreferredSize().height));
    }

}
