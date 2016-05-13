package training.ui;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.learn.LearnBundle;
import training.learn.Module;
import training.learn.CourseManager;
import training.learn.Lesson;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by karashevich on 26/06/15.
 */
public class MainLearnPanel extends JPanel {

    private int width;

    //UI Preferences
    private int north_inset;
    private int west_inset;
    private int south_inset;
    private int east_inset;
    private int check_width;
    private int check_right_indent;

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
    private JPanel submitFeedbackPanel;
    private LinkLabel submitFeedback;
    private int progressGap;

    private BidirectionalMap<Module, LinkLabel> module2linklabel;

    MainLearnPanel(int width) {
        super();
        module2linklabel = new BidirectionalMap<>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setFocusable(false);
        this.width = width;

        //Obligatory block
        generalizeUI();
        setOpaque(true);
        setBackground(getBackground());
        initMainPanel();
        add(lessonPanel);
        add(Box.createVerticalGlue());
        add(submitFeedbackPanel);

        //set LearnPanel UI
        this.setPreferredSize(new Dimension(width, 100));
        this.setBorder(new EmptyBorder(north_inset, west_inset, south_inset, east_inset));

        revalidate();
        repaint();

    }


    private void generalizeUI() {
        //generalize fonts, colors and sizes
        //TODO: change size to UiUtil size
        west_inset = 13;
        north_inset = 16;
        east_inset = 32;
        south_inset = 32;
        check_width = LearnIcons.CheckmarkGray12.getIconWidth();
        check_right_indent = 5;

        //UI colors and fonts
        fontSize = 12;
        moduleNameFont = new Font(UIUtil.getLabelFont().getName(), Font.BOLD, fontSize + 2);
        progressLabelFont = new Font(UIUtil.getLabelFont().getName(), Font.PLAIN, fontSize + 1);
        descriptionFont = new Font(UIUtil.getLabelFont().getName(), Font.PLAIN, fontSize + 1);
        descriptionColor = Gray._128;

        headerGap = 2;
        moduleGap = 20;
        progressGap = 12;


        StyleConstants.setFontFamily(REGULAR, descriptionFont.getFamily());
        StyleConstants.setFontSize(REGULAR, descriptionFont.getSize());
        StyleConstants.setForeground(REGULAR, descriptionColor);

        StyleConstants.setLeftIndent(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setRightIndent(PARAGRAPH_STYLE, 0);
        StyleConstants.setSpaceAbove(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setSpaceBelow(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setLineSpacing(PARAGRAPH_STYLE, 0.0f);
    }


    private void initMainPanel() {
        lessonPanel = new JPanel();
        lessonPanel.setLayout(new BoxLayout(lessonPanel, BoxLayout.PAGE_AXIS));
        lessonPanel.setOpaque(false);
        lessonPanel.setFocusable(false);
        initLessonPanel();
        submitFeedbackPanel = new JPanel();
        submitFeedbackPanel.setLayout(new BoxLayout(submitFeedbackPanel, BoxLayout.LINE_AXIS));
        submitFeedbackPanel.setOpaque(false);
        submitFeedbackPanel.setAlignmentX(LEFT_ALIGNMENT);
        submitFeedback = new LinkLabel(LearnBundle.message("learn.ui.mainpanel.submit.feedback"), null);
        submitFeedback.setListener((linkLabel, o) -> BrowserUtil.browse(LearnBundle.message("learn.ui.mainpanel.submit.feedback.url")), null);
        submitFeedbackPanel.add(Box.createHorizontalGlue());
        submitFeedbackPanel.add(submitFeedback);
    }

    private void initLessonPanel() {
        Module[] modules = CourseManager.getInstance().getModules();
        assert modules != null;
        for (Module module : modules) {
            JPanel moduleHeader = new JPanel();
            moduleHeader.setFocusable(false);
            moduleHeader.setAlignmentX(LEFT_ALIGNMENT);
            moduleHeader.setBorder(new EmptyBorder(0, check_width + check_right_indent, 0, 0));
            moduleHeader.setOpaque(false);
            moduleHeader.setLayout(new BoxLayout(moduleHeader, BoxLayout.X_AXIS));
            LinkLabel moduleName = new LinkLabel(module.getName(), null);
            module2linklabel.put(module, moduleName);
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
            moduleName.setAlignmentY(BOTTOM_ALIGNMENT);
            moduleName.setAlignmentX(LEFT_ALIGNMENT);
            String progressStr = calcProgress(module);
            JBLabel progressLabel;
            if (progressStr != null) {
                progressLabel = new JBLabel(progressStr);
            } else {
                progressLabel = new JBLabel();
            }
            progressLabel.setFont(progressLabelFont.deriveFont(Font.ITALIC));
            progressLabel.setForeground(JBColor.BLACK);
            progressLabel.setAlignmentY(BOTTOM_ALIGNMENT);
            moduleHeader.add(moduleName);
            moduleHeader.add(Box.createRigidArea(new Dimension(progressGap, 0)));
            moduleHeader.add(progressLabel);

            MyJTextPane descriptionPane = new MyJTextPane(width);
            descriptionPane.setEditable(false);
            descriptionPane.setOpaque(false);
            descriptionPane.setParagraphAttributes(PARAGRAPH_STYLE, true);
            try {
                String descriptionStr = module.getDescription();
                descriptionPane.getDocument().insertString(0, descriptionStr, REGULAR);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            descriptionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            descriptionPane.setMargin(new Insets(0, 0, 0, 0));
            descriptionPane.setBorder(new EmptyBorder(0, check_width + check_right_indent, 0, 0));
            descriptionPane.addMouseListener(delegateToLinkLabel(descriptionPane, moduleName));

            lessonPanel.add(moduleHeader);
            lessonPanel.add(Box.createVerticalStrut(headerGap));
            lessonPanel.add(descriptionPane);
            lessonPanel.add(Box.createVerticalStrut(moduleGap));
        }
        lessonPanel.add(Box.createVerticalGlue());
    }

    @NotNull
    private MouseListener delegateToLinkLabel(MyJTextPane descriptionPane, final LinkLabel moduleName) {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                moduleName.doClick();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                moduleName.doClick();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                moduleName.entered(e);
                descriptionPane.setCursor(Cursor.getPredefinedCursor(12));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                moduleName.exited(e);
                descriptionPane.setCursor(Cursor.getDefaultCursor());
            }
        };
    }

    public void updateMainPanel() {
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
            if (done == total) return "";
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
        return new Dimension((int) lessonPanel.getMinimumSize().getWidth() + (west_inset + east_inset),
                (int) lessonPanel.getMinimumSize().getHeight() + (north_inset + south_inset));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintModuleCheckmarks(g);
    }

    private void paintModuleCheckmarks(Graphics g) {
        if (module2linklabel != null || module2linklabel.size() > 0) {
            for (Module module : module2linklabel.keySet()) {
                if (module.giveNotPassedLesson() == null) {
                    final LinkLabel linkLabel = module2linklabel.get(module);
                    Point point = linkLabel.getLocationOnScreen();
                    final Point basePoint = this.getLocationOnScreen();
                    int y = point.y + 1 - basePoint.y;
                    LearnIcons.CheckmarkGray12.paintIcon(this, g, west_inset, y + 2);
                }
            }
        }
    }

    @Override
    public Color getBackground(){
        if (!UIUtil.isUnderDarcula()) return new Color(245, 245, 245);
        else return UIUtil.getPanelBackground();
    }

}

