package training.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;
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
public class MainLearnPanel extends JPanel {

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
    private Font progressLabelFont;
    private Color descriptionColor;
    private int fontSize;

    private JPanel lessonPanel;

    MainLearnPanel(int width) {
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

        //set LearnPanel UI
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
        progressLabelFont = new Font(UIUtil.getLabelFont().getName(), Font.PLAIN, fontSize + 1);
        descriptionFont = new Font(UIUtil.getLabelFont().getName(), Font.PLAIN, fontSize + 1);
        descriptionColor = Gray._128;

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
        initLessonPanel();
    }

    private void initLessonPanel() {
        Module[] modules = CourseManager.getInstance().getModules();
        assert modules != null;
        for (Module module : modules) {
            JPanel moduleHeader = new JPanel();
            moduleHeader.setBackground(background);
            moduleHeader.setFocusable(false);
            moduleHeader.setAlignmentX(LEFT_ALIGNMENT);

            moduleHeader.setLayout(new BoxLayout(moduleHeader, BoxLayout.LINE_AXIS));
            LinkLabel moduleName = new LinkLabel(module.getName(), null);
            moduleName.setListener((aSource, aLinkData) -> {
                try {
                    Project guessCurrentProject = ProjectUtil.guessCurrentProject(lessonPanel);
                    Lesson lesson = module.giveNotPassedLesson();
                    if (lesson == null) lesson = module.getLessons().get(0);
                    CourseManager.getInstance().openLesson(guessCurrentProject, lesson);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, null);
            moduleName.setFont(moduleNameFont);
            moduleName.setAlignmentX(LEFT_ALIGNMENT);
            String progressStr = calcProgress(module);
            JBLabel progressLabel;
            if (progressStr != null) {
                progressLabel = new JBLabel(progressStr);
            } else {
                progressLabel = new JBLabel();
            }
            progressLabel.setFont(progressLabelFont);
            progressLabel.setForeground(JBColor.BLACK);
            moduleHeader.add(moduleName);
            moduleHeader.add(Box.createRigidArea(new Dimension(10, 10)));
            moduleHeader.add(progressLabel);

            MyJTextPane descriptionPane = new MyJTextPane(width);
            descriptionPane.setEditable(false);
            descriptionPane.setParagraphAttributes(PARAGRAPH_STYLE, true);
            try {
                String descriptionStr = module.getDescription();
                descriptionPane.getDocument().insertString(0, descriptionStr, REGULAR);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            descriptionPane.setBackground(background);
            descriptionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            descriptionPane.setMargin(new Insets(0, 0, 0, 0));
            descriptionPane.setBorder(new EmptyBorder(0, 0, 0, 0));

            lessonPanel.add(moduleHeader);
            lessonPanel.add(Box.createRigidArea(new Dimension(0, headerGap)));
            lessonPanel.add(descriptionPane);
            lessonPanel.add(Box.createRigidArea(new Dimension(0, moduleGap)));
        }
        lessonPanel.add(Box.createVerticalGlue());
    }

    public void updateMainPanel(){
        lessonPanel.removeAll();
        initLessonPanel();
    }

    @Nullable
    private String calcProgress(Module module) {
        int total = module.getLessons().size();
        int done = 0;
        for (Lesson lesson : module.getLessons()) {
            if (lesson.getPassed()) done++;
        }
        if (done != 0) {
            if (done == total) return "Done";
            else return done + " of " + total + " done";
        } else {
            return null;
        }
    }

    private class MyJTextPane extends JTextPane {

        private int myWidth = 314;

        MyJTextPane(int widthOfText) {
            myWidth = widthOfText;
        }

        public Dimension getPreferredSize() {
            return new Dimension(myWidth, super.getPreferredSize().height);
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int) lessonPanel.getMinimumSize().getWidth() + 2 * insets, (int) lessonPanel.getMinimumSize().getHeight() + 2 * insets);
    }
}
