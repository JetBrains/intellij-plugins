package training.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import icons.LearnIcons;
import org.jetbrains.annotations.Nullable;
import training.learn.CourseManager;
import training.learn.LearnBundle;
import training.learn.Lesson;
import training.learn.Module;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

/**
 * Created by karashevich on 26/06/15.
 */
public class LearnPanel extends JPanel {

    private int width;

    //Lesson panel items
    private JPanel lessonPanel;
    private JLabel moduleNameLabel;
    private LinkLabel allTopicsLabel;

    private JLabel lessonNameLabel; //Name of the current lesson
    private LessonMessagePane lessonMessagePane;
    private JPanel buttonPanel;
    private JButton button;


    //UI Preferences
    private Color defaultTextColor; //default text color
    private int insets;
    private int north_inset;
    private int west_inset;
    private int south_inset;
    private int east_inset;
    private int check_width;
    private int check_right_indent;
    private EmptyBorder checkmarkShift;
    private JBColor background;

    //lessonPanel UI
    private int fontSize;
    private int lessonNameGap;
    private int beforeButtonGap;
    private int afterButtonGap;
    private int messageGap;
    private Font messageFont;
    private Font moduleNameFont;
    private Font allTopicsFont;
    private Font lessonNameFont;
    private JBColor shortcutTextColor;
    private JBColor shortcutBackgroundColor;
    private Color passedColor;
    private Color lessonPassedColor;
    private Color lessonCodeColor;
    private Color lessonLinkColor;


    //separator UI
    private Color separatorColor;
    private int separatorGap;

    //Module panel stuff
    private ModulePanel modulePanel;
    private JPanel moduleNamePanel; //contains moduleNameLabel and allTopicsLabel

    //modulePanel UI
    private int lessonGap;
    private Color lessonActiveColor;
    private Color lessonInactiveColor;
    private Font lessonsFont;
    private Font allLessonsFont;

    LearnPanel(int width) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setFocusable(false);
        this.width = width;

        //Obligatory block
        generalizeUI();
        initLessonPanel();
        initModulePanel();

        setOpaque(true);
        setBackground(getBackground());

        lessonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lessonPanel);
        modulePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(modulePanel);

        //set LearnPanel UI
        setPreferredSize(new Dimension(width, 100));
        setBorder(new EmptyBorder(north_inset, west_inset, south_inset, east_inset));
    }


    private void generalizeUI() {
        //generalize fonts, colors and sizes
        //TODO: change size to UiUtil size
        fontSize = 12;
        insets = 16;

        west_inset = 13;
        north_inset = 16;
        east_inset = 32;
        south_inset = 32;
        check_width = LearnIcons.CheckmarkGray12.getIconWidth();
        check_right_indent = 5;
        checkmarkShift = new EmptyBorder(0, check_width + check_right_indent, 0, 0);

        lessonNameGap = 5;
        beforeButtonGap = 20;
        afterButtonGap = 44;
        messageGap = 10;

        //separator UI
        separatorGap = 10;

        //§e UI
        lessonGap = 12;

        //UI colors and fonts
        moduleNameFont = new Font(JBUI.Fonts.label().getName(), Font.PLAIN, fontSize + 1);
        allTopicsFont = new Font(JBUI.Fonts.label().getName(), Font.PLAIN, fontSize + 1);
        lessonNameFont = new Font(UIUtil.getLabelFont().getName(), Font.BOLD, fontSize + 3);
        messageFont = new Font(JBUI.Fonts.label().getName(), Font.PLAIN, fontSize + 2);
        defaultTextColor = new JBColor(Gray._30, Gray._208);
        passedColor = new JBColor(Gray._105, Gray._103);
        lessonPassedColor = new JBColor(new Color(49, 140, 64), new Color(7, 140, 45));
        lessonCodeColor = new JBColor(new Color(27, 78, 128), new Color(85, 161, 255));
        lessonLinkColor = new JBColor(new Color(17, 96, 166), new Color(104, 159, 220));

        //shortcut UI
        shortcutTextColor = new JBColor(Gray._12, Gray._200);
        shortcutBackgroundColor = new JBColor(new Color(218, 226, 237), new Color(39, 43, 46));

        //separator UI
        separatorColor = new JBColor(new Color(204, 204, 204), Gray._149);

        //course UI
        lessonActiveColor = new JBColor(new Color(0, 0, 0), Gray._202);
        lessonInactiveColor = lessonLinkColor;
//        lessonsFont = new Font(UIUtil.getFont(fontSize, ), 0, fontSize);
        lessonsFont = new Font(JBUI.Fonts.label().getName(), Font.PLAIN, fontSize + 1);
        allLessonsFont = new Font(JBUI.Fonts.label().getName(), Font.BOLD, fontSize + 1);

    }


    private void initLessonPanel() {
        lessonPanel = new JPanel();
        lessonPanel.setLayout(new BoxLayout(lessonPanel, BoxLayout.Y_AXIS));
        lessonPanel.setFocusable(false);
        lessonPanel.setOpaque(false);

        moduleNameLabel = new JLabel();
        moduleNameLabel.setFont(moduleNameFont);
        moduleNameLabel.setFocusable(false);
        moduleNameLabel.setBorder(checkmarkShift);

        allTopicsLabel = new LinkLabel();
        allTopicsLabel.setFont(allTopicsFont);
        allTopicsLabel.setForeground(UIUtil.getActiveTextColor());
        allTopicsLabel.setFocusable(false);
        allTopicsLabel.setListener((aSource, aLinkData) -> {
            Project guessCurrentProject = ProjectUtil.guessCurrentProject(lessonPanel);
            CourseManager.getInstance().setModulesView(guessCurrentProject);
        }, null);

        lessonNameLabel = new JLabel();
        lessonNameLabel.setBorder(checkmarkShift);
        lessonNameLabel.setFont(lessonNameFont);
        lessonNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        lessonNameLabel.setFocusable(false);

        lessonMessagePane = new LessonMessagePane(fontSize + 1, check_width + check_right_indent);
        lessonMessagePane.setFocusable(false);
        lessonMessagePane.setOpaque(false);
        lessonMessagePane.setUI(defaultTextColor, shortcutTextColor, lessonCodeColor, lessonLinkColor, passedColor, shortcutBackgroundColor);
        lessonMessagePane.setAlignmentX(Component.LEFT_ALIGNMENT);
        lessonMessagePane.setMargin(new Insets(0, 0, 0, 0));
        lessonMessagePane.setBorder(new EmptyBorder(0, 0, 0, 0));
        lessonMessagePane.setMaximumSize(new Dimension(width, 10000));

        //Set Next Button UI
        button = new JButton(LearnBundle.message("learn.ui.button.skip"));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFocusable(false);
        button.setVisible(true);
        button.setEnabled(true);
        button.setOpaque(false);

        buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(0, check_width + check_right_indent, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setFocusable(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
        buttonPanel.add(button);

        lessonPanel.setName("lessonPanel");
        //shift right for checkmark
        lessonPanel.add(moduleNameLabel);
        lessonPanel.add(Box.createVerticalStrut(lessonNameGap));
        lessonPanel.add(lessonNameLabel);
//        lessonPanel.add(Box.createRigidArea(new Dimension(0, lessonNameGap)));
        lessonPanel.add(lessonMessagePane);
        lessonPanel.add(Box.createVerticalStrut(beforeButtonGap));
        lessonPanel.add(Box.createVerticalGlue());
        lessonPanel.add(buttonPanel);
        lessonPanel.add(Box.createVerticalStrut(afterButtonGap));
    }


    public void setLessonName(String lessonName) {
        lessonNameLabel.setText(lessonName);
        lessonNameLabel.setForeground(defaultTextColor);
        lessonNameLabel.setFocusable(false);
        this.revalidate();
        this.repaint();
    }

    public void setModuleName(String moduleName) {
        moduleNameLabel.setText(moduleName);
        moduleNameLabel.setForeground(defaultTextColor);
        moduleNameLabel.setFocusable(false);
        allTopicsLabel.setText(LearnBundle.message("learn.ui.alltopics"));
        allTopicsLabel.setIcon(null);
        this.revalidate();
        this.repaint();
    }


    public void addMessage(String text) {
        lessonMessagePane.addMessage(text);
    }

    public void addMessage(Message[] messages) {

        for (final Message message : messages) {
            if (message.getType() == Message.MessageType.LINK) {
                //add link handler
                message.setRunnable(() -> {
                    final Lesson lesson = CourseManager.getInstance().findLesson(message.getText());
                    if (lesson != null) {
                        try {
                            Project project = ProjectUtil.guessCurrentProject(LearnPanel.this);
                            CourseManager.getInstance().openLesson(project, lesson);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        lessonMessagePane.addMessage(messages);
        lessonMessagePane.revalidate();
        lessonMessagePane.repaint();

        //Pack lesson panel
        lessonPanel.revalidate();
        lessonPanel.repaint();
        this.revalidate();
        this.repaint();

    }

    public void setPreviousMessagesPassed() {
        try {
            lessonMessagePane.passPreviousMessages();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void setLessonPassed() {

//        lessonNameLabel.setForeground(lessonPassedColor);
        setButtonToNext();
        this.repaint();
    }

    private void setButtonToNext() {
        button.setVisible(true);
        lessonPanel.revalidate();
        lessonPanel.repaint();
//        button.requestFocus(true); focus requesting is danger here, may interfere with windows like File Structure
    }


    public void hideButtons() {
        if (button.isVisible()) button.setVisible(false);
        this.repaint();
    }

    public void clearLessonPanel() {
//        while (messages.size() > 0){
//            lessonMessageContainer.remove(messages.get(0).getPanel());
//            messages.remove(0);
//        }
//        lessonMessageContainer.removeAll();
        lessonNameLabel.setIcon(null);
        lessonMessagePane.clear();
        //remove links from lessonMessagePane
        final MouseListener[] mouseListeners = lessonMessagePane.getMouseListeners();
        for (MouseListener mouseListener : mouseListeners) {
            lessonMessagePane.removeMouseListener(mouseListener);
        }
//        messages.clear();
        this.revalidate();
        this.repaint();
    }


    public void setButtonNextAction(final Runnable runnable, Lesson notPassedLesson) {
        setButtonNextAction(runnable, notPassedLesson, null);
    }

    public void setButtonNextAction(final Runnable runnable, Lesson notPassedLesson, @Nullable String text) {

        Action buttonAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                runnable.run();
            }
        };

        int mnemonic_int = 'N';
        buttonAction.putValue(Action.MNEMONIC_KEY, mnemonic_int);
        buttonAction.putValue(Action.NAME, "Next");
        buttonAction.setEnabled(true);
        button.setAction(buttonAction);
        if (notPassedLesson != null) {
            if (text != null) {
                button.setText(text);
            } else {
                button.setText(LearnBundle.message("learn.ui.button.next.lesson") + ": " + notPassedLesson.getName());
            }
        } else {
            button.setText(LearnBundle.message("learn.ui.button.next.lesson"));
        }
        button.setSelected(true);
        getRootPane().setDefaultButton(button);
    }

    public void setButtonSkipAction(final Runnable runnable, @Nullable String text, boolean visible) {

        if (getRootPane() != null)
            getRootPane().setDefaultButton(null);
        Action buttonAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                runnable.run();
            }
        };

        buttonAction.setEnabled(true);
        button.setAction(buttonAction);
        if (text == null || text.isEmpty()) button.setText(LearnBundle.message("learn.ui.button.skip"));
        else button.setText(LearnBundle.message("learn.ui.button.skip.module") + " "+ text);
        button.setSelected(true);
        button.setVisible(visible);
    }


    public void hideNextButton() {
        button.setVisible(false);
    }

    private void initModulePanel() {
        modulePanel = new ModulePanel();
        modulePanel.setLayout(new BoxLayout(modulePanel, BoxLayout.Y_AXIS));
        modulePanel.setFocusable(false);
        modulePanel.setOpaque(false);

        //define separator
        modulePanel.setBorder(new MatteBorder(1, 0, 0, 0, separatorColor));
    }

    public ModulePanel getModulePanel() {
        return modulePanel;
    }

    public void clear() {
        clearLessonPanel();
        //clearModulePanel
        modulePanel.removeAll();
    }

    public void updateButtonUi() {
        button.updateUI();
    }

    public class ModulePanel extends JPanel {
        private Module myModule;
        private Lesson myLesson;
        private BidirectionalMap<Lesson, MyLinkLabel> lessonLabelMap = new BidirectionalMap<Lesson, MyLinkLabel>();

        public void init(Lesson lesson) {

            myLesson = lesson;
            myModule = lesson.getModule();
            initModuleLessons(lesson);
        }

        private void initModuleLessons(final Lesson lesson) {
            if (lesson == null) return;
            if (lesson.getModule() == null) return;
            Module module = lesson.getModule();
            final ArrayList<Lesson> myLessons = module.getLessons();

            //if module contains one lesson only
            if (myLessons.size() == 1) return;

            //create ModuleLessons region
            JLabel moduleLessons = new JLabel();

            moduleNamePanel = new JPanel();
            moduleNamePanel.setBorder(new EmptyBorder(lessonGap, check_width + check_right_indent, 0, 0));
            moduleNamePanel.setOpaque(false);
            moduleNamePanel.setFocusable(false);
            moduleNamePanel.setLayout(new BoxLayout(moduleNamePanel, BoxLayout.X_AXIS));
            moduleNamePanel.setAlignmentX(LEFT_ALIGNMENT);
            moduleNamePanel.add(moduleLessons);
            moduleNamePanel.add(Box.createHorizontalStrut(20));
            moduleNamePanel.add(Box.createHorizontalGlue());
            moduleNamePanel.add(allTopicsLabel);

            moduleLessons.setText(lesson.getModule().getName());
            moduleLessons.setFont(allLessonsFont);
            moduleLessons.setFocusable(false);

            add(Box.createRigidArea(new Dimension(0, 5)));
            add(moduleNamePanel);
            add(Box.createRigidArea(new Dimension(0, 10)));

            buildLessonLabels(lesson, myLessons);
            setMaximumSize(new Dimension(width, modulePanel.getPreferredSize().height));
        }

        private void buildLessonLabels(Lesson lesson, final ArrayList<Lesson> myLessons) {
            for (final Lesson currentLesson : myLessons) {
                String lessonName = currentLesson.getName();

                final MyLinkLabel e = new MyLinkLabel(lessonName);
                e.setHorizontalTextPosition(SwingConstants.LEFT);
                e.setBorder(new EmptyBorder(0, check_width + check_right_indent, lessonGap, 0));
                e.setFocusable(false);
                e.setListener((aSource, aLinkData) -> {
                    try {
                        Project project = ProjectUtil.guessCurrentProject(LearnPanel.this);
                        CourseManager.getInstance().openLesson(project, currentLesson);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }, null);

                if (lesson.equals(currentLesson)) {
                    //selected lesson
                    e.setTextColor(lessonActiveColor);
                } else {
                    e.resetTextColor();
                }
                e.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                lessonLabelMap.put(currentLesson, e);
                add(e);
            }
        }

        public void updateLessons(Lesson lesson) {
            for (Lesson curLesson : lessonLabelMap.keySet()) {
                MyLinkLabel lessonLabel = lessonLabelMap.get(curLesson);
                if (lesson.equals(curLesson)) {
                    lessonLabel.setTextColor(lessonActiveColor);
                } else {
                    lessonLabel.resetTextColor();
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            paintModuleCheckmarks(g);
        }

        private void paintModuleCheckmarks(Graphics g) {
            for (Lesson lesson : lessonLabelMap.keySet()) {
                if (lesson.getPassed()) {
                    JLabel jLabel = lessonLabelMap.get(lesson);
                    Point point = jLabel.getLocation();
                    LearnIcons.CheckmarkGray12.paintIcon(this, g, point.x, point.y + 2);
                }
            }
        }


        class MyLinkLabel extends LinkLabel {

            Color userTextColor;

            MyLinkLabel(String text) {
                super(text, null);
            }

            @Override
            protected Color getTextColor() {
                return (userTextColor != null ? userTextColor : super.getTextColor());
            }

            void setTextColor(Color color) {
                userTextColor = color;
            }

            void resetTextColor() {
                userTextColor = null;
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int) lessonPanel.getMinimumSize().getWidth() + 2 * insets, (int) lessonPanel.getMinimumSize().getHeight() + (int) modulePanel.getMinimumSize().getHeight() + 2 * insets);
    }

    @Override
    public Color getBackground(){
        if (!UIUtil.isUnderDarcula()) return new Color(245, 245, 245);
        else return super.getBackground();
    }
}
