package org.jetbrains.training.editor.eduUI;

import com.intellij.util.ui.UIUtil;
import org.jetbrains.training.lesson.BadCourseException;
import org.jetbrains.training.lesson.BadLessonException;
import org.jetbrains.training.lesson.LessonIsOpenedException;
import org.jetbrains.training.editor.EduEditor;
import org.jetbrains.training.lesson.Course;
import org.jetbrains.training.lesson.CourseManager;
import org.jetbrains.training.lesson.Lesson;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 26/06/15.
 */
public class EduPanel extends JPanel {

    private EduEditor eduEditor;
    private int width;

    //Lesson panel items
    private JPanel lessonPanel;
    private JLabel lessonNameLabel; //Name of the current lesson
    @Deprecated private JPanel lessonMessageContainer; //Contains lesson's messages
    private LessonMessagePane lessonMessagePane;
    private JButton lessonNextButton;
    private ArrayList<LessonMessage> messages;


    //UI Preferences
    private Color background;
    private Color defaultTextColor; //default text color
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
    private Color lessonPassedColor;
    private Color lessonCodeColor;

    //separator UI
    private Color separatorColor;
    private int separatorGap;

    //Course panel stuff
    private JPanel coursePanel;
    private ArrayList<JLabel> lessonsLabels;

    //coursePanel UI
    private int lessonGap;
    private Color lessonActiveColor;
    private Color lessonInactiveColor;
    private Font lessonsFont;
    private Font allLessonsFont;


    public EduPanel(EduEditor eduEditor, int width){
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.width = width;
        this.eduEditor = eduEditor;

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
//        addMessage("<html>Comment out any line 23u5 35" +
//                "23 45" +
//                "23 4" +
//                " 234" +
//                "2346 34 623462346236 346234 6 2346with <b>Ctrl + /</b> </html>");
//        setPreviousMessagesPassed();
//        addMessage("<html>Comment the line with</html>");
//        setPreviousMessagesPassed();
//        addMessage("<html>Comment the line with the same shortcut</html>");
//        setPreviousMessagesPassed();
//        addMessage("<html>Comment the line with the same shortcut</html>");
//        addMessage("<html>Comment the line with the same shortcut</html>");


    }

    private void generalizeUI() {
        //generalize fonts, colors and sizes
        //TODO: change size to UiUtil size
        fontSize = 12;
        insets = 10;
        lessonNameGap = 10;
        lessonNextButtonGap = 10;
        messageGap = 10;

        //separator UI
        separatorGap = 10;

        //course UI
        lessonGap = 10;

        //Look and feel differences
        if (!UIUtil.isUnderDarcula()) {
            background = new Color(250, 250, 250);
            lessonNameFont = new Font(UIUtil.getLabelFont().getName(), Font.BOLD, fontSize);
            messageFont = new Font(UIUtil.getLabelFont().getName(), 0, fontSize);
            defaultTextColor = new Color(30, 30, 30);
            passedColor = new Color(105, 105, 105);
            lessonPassedColor = new Color(49, 140, 64);
            lessonCodeColor = new Color(31, 55, 128);

            //shortcut UI
            shortcutTextColor = new Color(12, 12, 12);

            //separator UI
            separatorColor = new Color(222, 222, 222);

            //course UI
            lessonActiveColor = new Color(167, 167, 167);
            lessonInactiveColor = new Color(17, 96, 166);
            lessonsFont = new Font(UIUtil.getLabelFont().getName(), 0, fontSize);
            allLessonsFont = new Font(UIUtil.getLabelFont().getName(), 0, fontSize - 1);

        } else  {

            background = new Color(50, 50, 50);
            lessonNameFont = new Font(UIUtil.getLabelFont().getName(), Font.BOLD, fontSize);
            messageFont = new Font(UIUtil.getLabelFont().getName(), 0, fontSize);
            defaultTextColor = new Color(208, 208, 208);
            passedColor = new Color(103, 103, 103);
            lessonPassedColor = new Color(7, 140, 45);
            lessonCodeColor = new Color(85, 161, 255);

            //shortcut UI
            shortcutTextColor = new Color(200, 200, 200);


            //separator UI
            separatorColor = new Color(149, 149, 149);

            //course UI
            lessonActiveColor = new Color(202, 202, 202);
            lessonInactiveColor = new Color(104, 159, 220);
            lessonsFont = new Font(UIUtil.getLabelFont().getName(), 0, fontSize);
            allLessonsFont = new Font(UIUtil.getLabelFont().getName(), 0, fontSize - 1);
        }

    }



    private void initLessonPanel(){
        lessonPanel = new JPanel();
        lessonPanel.setLayout(new BoxLayout(lessonPanel, BoxLayout.Y_AXIS));
        lessonPanel.setBackground(background);

        lessonNameLabel = new JLabel("Comment");
        lessonNameLabel.setFont(lessonNameFont);
        lessonNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

//        lessonMessageContainer = new JPanel();
//        messages = new ArrayList<LessonMessage>();
//        lessonMessageContainer.setLayout(new BoxLayout(lessonMessageContainer, BoxLayout.Y_AXIS));
//        lessonMessageContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        lessonMessagePane = new LessonMessagePane();
        lessonMessagePane.setFocusable(false);
        //Set lessonMessagePane UI
        lessonMessagePane.setBackground(background);
        lessonMessagePane.setUI(defaultTextColor, shortcutTextColor, lessonCodeColor, passedColor);

        //Set label UI
        lessonNextButton = new JButton("Next");
        lessonNextButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        lessonNextButton.setVisible(false);
        lessonNextButton.setBackground(background);


        lessonPanel.add(lessonNameLabel);
        lessonPanel.add(Box.createRigidArea(new Dimension(0, lessonNameGap)));
        lessonPanel.add(lessonMessagePane);
        lessonPanel.add(Box.createRigidArea(new Dimension(0, lessonNextButtonGap)));
        lessonPanel.add(lessonNextButton);
    }


    public void setLessonName(String lessonName){
        lessonNameLabel.setText(lessonName);
        lessonNameLabel.setForeground(defaultTextColor);
        this.revalidate();
        this.repaint();
    }

//    public void addMessage(String text){
//        JLabel newLabel = new JLabel(XmlUtil.addHtmlTags(text));
//        newLabel.setFont(messageFont);
//        final LessonMessage lessonMessage = new LessonMessage(newLabel, null);
//        messages.add(lessonMessage);
//        final JPanel lessonMessagePanel = lessonMessage.getPanel();
//        lessonMessagePanel.setBackground(background);
//        lessonMessagePanel.setBorder(new EmptyBorder(0, 0, messageGap, 0));
//
//        lessonMessageContainer.add(lessonMessagePanel);
//
//        //Pack lesson panel
//        lessonPanel.revalidate();
//        this.revalidate();
//        this.repaint();
//    }

    public void addMessage(String text){
        lessonMessagePane.addMessage(text);

        //Pack lesson panel
        lessonPanel.revalidate();
        this.revalidate();
        this.repaint();
    }

    public void addMessage(Message[] messages) {
        lessonMessagePane.addMessage(messages);

        //Pack lesson panel
        lessonPanel.revalidate();
        this.revalidate();
        this.repaint();

    }


//    public void addMessage(String text, String shortcut){
//        JLabel newLabel = new JLabel(XmlUtil.addHtmlTags(text));
//        newLabel.setFont(messageFont);
//        final LessonMessage lessonMessage = new LessonMessage(newLabel, new ShortcutLabel(shortcut,shortcutFont, shortcutTextColor, shortcutBckColor, shortcutBorderColor));
//        messages.add(lessonMessage);
//        final JPanel lessonMessagePanel = lessonMessage.getPanel();
//        lessonMessagePanel.setBackground(background);
//        lessonMessagePanel.setBorder(new EmptyBorder(0, 0, messageGap, 0));
//
//        lessonMessageContainer.add(lessonMessagePanel);
//        this.revalidate();
//        this.repaint();
//    }

    public void setPreviousMessagesPassed(){
        lessonMessagePane.passPreviousMessages();
//        if (messages == null) return;
//        for (LessonMessage message : messages) {
////            message.setForeground(passedColor);
//            if (message.getLabel().getForeground() != passedColor) {
//                message.getLabel().setForeground(passedColor);
//            }
//        }
//
//        //add to last message check mark
//        LessonMessage lastMessage = messages.get(messages.size() - 1);
//        String text = XmlUtil.removeHtmlTags(lastMessage.getLabel().getText());
//        text += " ✔";
//        lastMessage.getLabel().setText(XmlUtil.addHtmlTags(text));
//
    }

    public void setLessonPassed(){
        lessonNameLabel.setText(lessonNameLabel.getText() + " ✔");
        lessonNameLabel.setForeground(lessonPassedColor);
        lessonNextButton.setVisible(true);
        this.repaint();
    }


    private void initCoursePanel(){
        coursePanel = new JPanel();
        coursePanel.setLayout(new BoxLayout(coursePanel, BoxLayout.Y_AXIS));
        coursePanel.setBackground(background);

        //define separator
        coursePanel.setBorder(new MatteBorder(1, 0, 0, 0, separatorColor));

        lessonsLabels = new ArrayList<JLabel>();

    }


    public void setAllLessons(final Lesson lesson){
        if(lesson == null) return;
        if(lesson.getCourse() == null) return;
        Course course = lesson.getCourse();
        final ArrayList<Lesson> myLessons = course.getLessons();

        //if course contains one lesson only
        if (myLessons.size() == 1) return;
        //cleat AllLessons region
        if(lessonsLabels.size() > 0) {
            while (lessonsLabels.size() > 0){
                coursePanel.remove(lessonsLabels.get(0));
                lessonsLabels.remove(0);
            }


        } else {
            JLabel allLessons = new JLabel();
            allLessons.setText("ALL LESSONS");
            allLessons.setFont(allLessonsFont);

            coursePanel.add(Box.createRigidArea(new Dimension(0, 5)));
            coursePanel.add(allLessons);
            coursePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        for (int i = 0; i < myLessons.size(); i++) {
            Lesson currentLesson = myLessons.get(i);
            String id = currentLesson.getId();
            if (currentLesson.isPassed()) id = id + " ✔";

            if (lesson.equals(currentLesson)){
                //selected lesson
                final JLabel e = new JLabel(id);
                e.setForeground(lessonActiveColor);
                e.setBorder(new EmptyBorder(0, 0, lessonGap, 0));
                e.setFont(lessonsFont);
                lessonsLabels.add(e);
                coursePanel.add(e);
            } else {
                final JLabel e = new JLabel(id);
                e.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                e.setForeground(lessonInactiveColor);
                e.setBorder(new EmptyBorder(0, 0, lessonGap, 0));
                e.setFont(lessonsFont);
                final int index = i;
                e.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent mouseEvent) {
                        try {
                            CourseManager.getInstance().openLesson(eduEditor.getEditor().getProject(), myLessons.get(index));
                        } catch (BadCourseException e1) {
                            e1.printStackTrace();
                        } catch (BadLessonException e1) {
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (FontFormatException e1) {
                            e1.printStackTrace();
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        } catch (ExecutionException e1) {
                            e1.printStackTrace();
                        } catch (LessonIsOpenedException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
                lessonsLabels.add(e);
                coursePanel.add(e);
            }
        }
        coursePanel.setMaximumSize(new Dimension(width - insets * 2, coursePanel.getPreferredSize().height));
    }

    public void hideButtons(){
        if (lessonNextButton.isVisible()) lessonNextButton.setVisible(false);
        this.repaint();
    }

    public void clearLessonPanel() {
//        while (messages.size() > 0){
//            lessonMessageContainer.remove(messages.get(0).getPanel());
//            messages.remove(0);
//        }
//        lessonMessageContainer.removeAll();
        lessonMessagePane.clear();
//        messages.clear();
        this.revalidate();
        this.repaint();
    }


    public void updateLessonPanel(Lesson lesson){
        setAllLessons(lesson);
    }

    public void setNextButtonAction(final Runnable runnable) {

        //remove previous action listeners
        ActionListener[] actionListeners = lessonNextButton.getActionListeners();
        for (int i = 0; i < actionListeners.length; i++) {
            lessonNextButton.removeActionListener(actionListeners[i]);
        }

        final ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                runnable.run();
                lessonNextButton.removeActionListener(this);
            }
        };
        lessonNextButton.addActionListener(actionListener);
    }

    public void hideNextButton() {
        lessonNextButton.setVisible(false);
    }

    public static void main(String[] args) {
        EduPanel eduPanel = new EduPanel(null, 350);
        JFrame frame = new JFrame("Test EduPanel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(eduPanel);
        frame.setSize(350, 800);
        frame.setVisible(true);

        eduPanel.addMessage("Hey!");
    }

}
