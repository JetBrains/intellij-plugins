package training.editor.eduUI;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import com.intellij.util.ui.UIUtil;
import training.learn.Module;
import training.learn.CourseManager;
import training.learn.Lesson;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

/**
 * Created by karashevich on 26/06/15.
 */
public class MainEduPanel extends JPanel {

    private int width;

    //UI Preferences
    private Color background;
    private int insets;
    private static SimpleAttributeSet REGULAR = new SimpleAttributeSet();
    private static SimpleAttributeSet PARAGRAPH_STYLE = new SimpleAttributeSet();
    private int headerGap;
    private int moduleGap;
    private Font moduleNameFont;
    private Font descriptionFont;
    private Color descriptionColor;
    private int fontSize;

    private JPanel lessonPanel;

    public MainEduPanel(int width){
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setFocusable(false);
        this.width = width;

        //Obligatory block
        generalizeUI();
        this.setBackground(background);
        initMainPanel();
        add(lessonPanel);
        add(Box.createVerticalGlue());

        //set EduPanel UI
        this.setPreferredSize(new Dimension(width, 100));
        this.setBorder(new EmptyBorder(insets, insets, insets, insets));
        revalidate();
        repaint();

    }


    private void generalizeUI() {
        //generalize fonts, colors and sizes
        //TODO: change size to UiUtil size
        insets = 16;

        //UI colors and fonts
        fontSize = 12;
        background = new JBColor(Gray._250, Gray._50);
        moduleNameFont = new Font(UIUtil.getLabelFont().getName(), Font.BOLD, fontSize + 2);
        descriptionFont = new Font(UIUtil.getLabelFont().getName(), Font.PLAIN, fontSize + 1);
        descriptionColor = new Color(128, 128, 128);

        headerGap = 8;
        moduleGap = 20;

        setBackground(background);

        StyleConstants.setFontFamily(REGULAR, descriptionFont.getFamily());
        StyleConstants.setFontSize(REGULAR, descriptionFont.getSize());
        StyleConstants.setForeground(REGULAR, descriptionColor);
        StyleConstants.setBackground(REGULAR, background);

        StyleConstants.setLeftIndent(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setRightIndent(PARAGRAPH_STYLE, 0);
        StyleConstants.setSpaceAbove(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setSpaceBelow(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setLineSpacing(PARAGRAPH_STYLE, 0.0f);
    }


    private void initMainPanel() {
        lessonPanel = new JPanel();
        lessonPanel.setLayout(new BoxLayout(lessonPanel, BoxLayout.PAGE_AXIS));
        lessonPanel.setBackground(background);
        lessonPanel.setFocusable(false);

        Module[] modules = CourseManager.getInstance().getModules();
        for (Module module : modules) {
            LinkLabel moduleName = new LinkLabel(module.getName(), null);
            moduleName.setListener(new LinkListener() {
                @Override
                public void linkSelected(LinkLabel aSource, Object aLinkData) {
                    try {
                        Project guessCurrentProject = ProjectUtil.guessCurrentProject(lessonPanel);
                        Lesson lesson = module.giveNotPassedLesson();
                        CourseManager.getInstance().setLessonView(guessCurrentProject);
                        CourseManager.getInstance().openLesson(guessCurrentProject, lesson);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, null);
            moduleName.setFont(moduleNameFont);
            moduleName.setAlignmentX(LEFT_ALIGNMENT);
            MyJTextPane description = new MyJTextPane(width);
            description.setEditable(false);
            description.setParagraphAttributes(PARAGRAPH_STYLE, true);
            try {
                description.getDocument().insertString(0, "Learn how to select, move, comment and duplicate line in one shortcut. Want to get more info? Just click a link!", REGULAR);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            description.setBackground(background);
            description.setAlignmentX(Component.LEFT_ALIGNMENT);
            description.setMargin(new Insets(0, 0, 0, 0));
            description.setBorder(new EmptyBorder(0, 0, 0, 0));

            lessonPanel.add(moduleName);
            lessonPanel.add(Box.createRigidArea(new Dimension(0, headerGap)));
            lessonPanel.add(description);
            lessonPanel.add(Box.createRigidArea(new Dimension(0, moduleGap)));
        }
        lessonPanel.add(Box.createVerticalGlue());
    }

    private class MyJTextPane extends JTextPane {

        private int myWidth = 314;

        MyJTextPane(int widthOfText){
            myWidth = widthOfText;
        }

        public Dimension getPreferredSize() {
            return new Dimension(myWidth,super.getPreferredSize().height);
        }
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }



}
