package org.jetbrains.training.eduUI.panel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by karashevich on 26/06/15.
 */
public class EduPanel extends JPanel {

    private int width;

    //Lesson panel stuff
    private JPanel lessonPanel;
    private JLabel lessonNameLabel; //Name of the current lesson
    private JLabel lessonLabel; //Lesson description
    private JButton lessonNextButton;
    private int fontSize;

    private JPanel coursePanel;

    private Color background;


    public EduPanel(int width){
        super(new BorderLayout());
        this.width = width;

        //Obligatory block
        generalizeUI();
        initLessonPanel();
        initCoursePanel();

        this.add(lessonPanel, BorderLayout.NORTH);
        this.add(coursePanel, BorderLayout.SOUTH);

        //Pack lesson panel
        lessonPanel.revalidate();
        this.revalidate();
        this.repaint();
    }

    private void generalizeUI(){
        //generalize fonts, colors and sizes
    }

    private void initLessonPanel(){
        lessonPanel = new JPanel();
        lessonPanel.setLayout(new BoxLayout(lessonPanel, BoxLayout.Y_AXIS));

        lessonNameLabel = new JLabel("Comment");
        lessonLabel = new JLabel();
        lessonLabel.setText("<html>Comment out any line with Ctrl+/ <br>" +
                "Comment the line with the same shortcut</html>");
        lessonNextButton = new JButton("Next");

        lessonPanel.add(lessonNameLabel);
        lessonPanel.add(lessonLabel);
        lessonPanel.add(lessonNextButton);
    }

    private void initCoursePanel(){
        coursePanel = new JPanel();
    }

}
