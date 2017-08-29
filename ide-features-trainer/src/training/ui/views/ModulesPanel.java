package training.ui.views;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.learn.CourseManager;
import training.learn.Module;
import training.learn.lesson.Lesson;
import training.ui.LearnIcons;
import training.ui.LearnUISettings;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

/**
 * Created by karashevich on 26/06/15.
 */
public class ModulesPanel extends JPanel {

    private static SimpleAttributeSet REGULAR = new SimpleAttributeSet();
    private static SimpleAttributeSet PARAGRAPH_STYLE = new SimpleAttributeSet();

    private JPanel lessonPanel;

    private BidirectionalMap<Module, LinkLabel> module2linklabel;

    public ModulesPanel() {
        super();
        module2linklabel = new BidirectionalMap<>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setFocusable(false);

        //Obligatory block
        generalizeUI();
        setOpaque(true);
        setBackground(getBackground());
        initMainPanel();
        add(lessonPanel);
        add(Box.createVerticalGlue());

        //set LearnPanel UI
        this.setPreferredSize(new Dimension(LearnUISettings.getInstance().getWidth(), 100));
        this.setBorder(LearnUISettings.getInstance().getEmptyBorder());

        revalidate();
        repaint();

    }


    private void generalizeUI() {

        StyleConstants.setFontFamily(REGULAR, LearnUISettings.getInstance().getFontFace());
        StyleConstants.setFontSize(REGULAR, LearnUISettings.getInstance().getFontSize());
        StyleConstants.setForeground(REGULAR, LearnUISettings.getInstance().getDescriptionColor());

        StyleConstants.setLeftIndent(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setRightIndent(PARAGRAPH_STYLE, 0);
        StyleConstants.setSpaceAbove(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setSpaceBelow(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setLineSpacing(PARAGRAPH_STYLE, 0.0f);
    }


    private void initMainPanel() {
        lessonPanel = new JPanel();
        lessonPanel.setName("lessonPanel");
        lessonPanel.setLayout(new BoxLayout(lessonPanel, BoxLayout.PAGE_AXIS));
        lessonPanel.setOpaque(false);
        lessonPanel.setFocusable(false);
        initModulesPanel();
    }

    private void initModulesPanel() {
        List<Module> modules = CourseManager.Companion.getInstance().getModules();
        for (Module module : modules) {
            if (module.getLessons().size() == 0) continue;
            JPanel moduleHeader = new JPanel();
            moduleHeader.setFocusable(false);
            moduleHeader.setAlignmentX(LEFT_ALIGNMENT);
            moduleHeader.setBorder(LearnUISettings.getInstance().getCheckmarkShiftBorder());
            moduleHeader.setOpaque(false);
            moduleHeader.setLayout(new BoxLayout(moduleHeader, BoxLayout.X_AXIS));
            LinkLabel moduleName = new LinkLabel(module.getName(), null);
            module2linklabel.put(module, moduleName);
            moduleName.setListener((aSource, aLinkData) -> {
                try {
                    Project guessCurrentProject = ProjectUtil.guessCurrentProject(lessonPanel);
                    Lesson lesson = module.giveNotPassedLesson();
                    if (lesson == null) lesson = module.getLessons().get(0);
                    CourseManager.Companion.getInstance().openLesson(guessCurrentProject, lesson);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, null);
            moduleName.setFont(LearnUISettings.getInstance().getModuleNameFont());
            moduleName.setAlignmentY(BOTTOM_ALIGNMENT);
            moduleName.setAlignmentX(LEFT_ALIGNMENT);
            String progressStr = calcProgress(module);
            JBLabel progressLabel;
            if (progressStr != null) {
                progressLabel = new JBLabel(progressStr);
            } else {
                progressLabel = new JBLabel();
            }
            progressLabel.setFont(LearnUISettings.getInstance().getItalicFont());
            progressLabel.setForeground(JBColor.BLACK);
            progressLabel.setAlignmentY(BOTTOM_ALIGNMENT);
            moduleHeader.add(moduleName);
            moduleHeader.add(Box.createRigidArea(new Dimension(LearnUISettings.getInstance().getProgressGap(), 0)));
            moduleHeader.add(progressLabel);

            MyJTextPane descriptionPane = new MyJTextPane(LearnUISettings.getInstance().getWidth());
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
            descriptionPane.setBorder(LearnUISettings.getInstance().getCheckmarkShiftBorder());
            descriptionPane.addMouseListener(delegateToLinkLabel(descriptionPane, moduleName));

            lessonPanel.add(moduleHeader);
            lessonPanel.add(Box.createVerticalStrut(LearnUISettings.getInstance().getHeaderGap()));
            lessonPanel.add(descriptionPane);
            lessonPanel.add(Box.createVerticalStrut(LearnUISettings.getInstance().getModuleGap()));
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
        initModulesPanel();
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
        return new Dimension((int) lessonPanel.getMinimumSize().getWidth() + (LearnUISettings.getInstance().getWestInset() + LearnUISettings.getInstance().getWestInset()),
                (int) lessonPanel.getMinimumSize().getHeight() + (LearnUISettings.getInstance().getNorthInset() + LearnUISettings.getInstance().getSouthInset()));
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
                    if (!SystemInfo.isMac) {
                        LearnIcons.INSTANCE.getCheckMarkGray().paintIcon(this, g, LearnUISettings.getInstance().getWestInset(), y + 4);
                    } else {
                        LearnIcons.INSTANCE.getCheckMarkGray().paintIcon(this, g, LearnUISettings.getInstance().getWestInset(), y + 2);
                    }
                }
            }
        }
    }


    @Override
    public Color getBackground() {
        if (!UIUtil.isUnderDarcula()) return LearnUISettings.getInstance().getBackgroundColor();
        else return UIUtil.getPanelBackground();
    }

}

