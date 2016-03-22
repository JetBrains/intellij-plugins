package training.editor.eduUI;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import icons.LearnIcons;
import training.learn.Course;
import training.learn.CourseManager;
import training.learn.EducationBundle;
import training.learn.Lesson;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

/**
 * Created by karashevich on 26/06/15.
 */
public class EduPanel extends JPanel {

    private int width;

    //Lesson panel items
    private JPanel lessonPanel;

    private JPanel moduleNamePanel; //contains moduleNameLabel and allTopicsLabel
    private JLabel moduleNameLabel;
    private JLabel allTopicsLabel;

    private JLabel lessonNameLabel; //Name of the current lesson
    private LessonMessagePane lessonMessagePane;
    private JButton lessonNextButton;
    private JButton lessonNextTestButton;


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
    private Font moduleNameFont;
    private Font allTopicsFont;
    private Font lessonNameFont;
    private Color shortcutTextColor;
    private Color passedColor;
    private Color lessonPassedColor;
    private Color lessonCodeColor;
    private Color lessonLinkColor;


    //separator UI
    private Color separatorColor;
    private int separatorGap;

    //Course panel stuff
    private ModulePanel modulePanel;


    //modulePanel UI
    private int lessonGap;
    private Color lessonActiveColor;
    private Color lessonInactiveColor;
    private Font lessonsFont;
    private Font allLessonsFont;

    public EduPanel(int width){
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setFocusable(false);
        this.width = width;

        //Obligatory block
        generalizeUI();
        this.setBackground(background);
        initLessonPanel();
        initModulePanel();

        lessonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(lessonPanel);
        final Box.Filler filler = new Box.Filler(
                new Dimension(5, separatorGap),    //minimum size of filler
                new Dimension(5, separatorGap),    //preferable size of filler
                new Dimension(5, Short.MAX_VALUE)     //maximum size of filler
        );
        filler.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(filler);
        modulePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(modulePanel);

        //set EduPanel UI
        this.setPreferredSize(new Dimension(width, 100));
        this.setBorder(new EmptyBorder(insets, insets, insets, insets));

    }


    private void generalizeUI() {
        //generalize fonts, colors and sizes
        //TODO: change size to UiUtil size
        fontSize = 12;
        insets = 16;
        lessonNameGap = 4;
        lessonNextButtonGap = 10;
        messageGap = 10;

        //separator UI
        separatorGap = 10;

        //§e UI
        lessonGap = 10;

        //UI colors and fonts
        background = new JBColor(Gray._250, Gray._50);
        moduleNameFont = new Font(JBUI.Fonts.label().getName(), Font.PLAIN, fontSize + 1);
        allTopicsFont = new Font(JBUI.Fonts.label().getName(), Font.PLAIN, fontSize + 1);
        lessonNameFont = new Font(UIUtil.getLabelFont().getName(), Font.BOLD, fontSize + 2);
        messageFont = new Font(JBUI.Fonts.label().getName(), Font.PLAIN, fontSize + 1);
        defaultTextColor = new JBColor(Gray._30, Gray._208);
        passedColor = new JBColor(Gray._105, Gray._103);
        lessonPassedColor = new JBColor(new Color(49, 140, 64), new Color(7, 140, 45));
        lessonCodeColor = new JBColor(new Color(27, 78, 128), new Color(85, 161, 255));
        lessonLinkColor = new JBColor(new Color(17, 96, 166), new Color(104, 159, 220));

        //shortcut UI
        shortcutTextColor = new JBColor(Gray._12, Gray._200);

        //separator UI
        separatorColor = new JBColor(Gray._222, Gray._149);

        //course UI
        lessonActiveColor = new JBColor(Gray._167, Gray._202);
        lessonInactiveColor = lessonLinkColor;
//        lessonsFont = new Font(UIUtil.getFont(fontSize, ), 0, fontSize);
        lessonsFont = new Font(JBUI.Fonts.label().getName(), Font.PLAIN, fontSize + 1);;
        allLessonsFont = new Font(JBUI.Fonts.label().getName(), Font.BOLD, fontSize + 1);

    }



    private void initLessonPanel(){
        lessonPanel = new JPanel();
        lessonPanel.setLayout(new BoxLayout(lessonPanel, BoxLayout.Y_AXIS));
        lessonPanel.setBackground(background);
        lessonPanel.setFocusable(false);

        moduleNamePanel = new JPanel();
        moduleNamePanel.setBackground(background);
        moduleNamePanel.setFocusable(false);
        moduleNamePanel.setLayout(new BoxLayout(moduleNamePanel, BoxLayout.X_AXIS));
        moduleNamePanel.setAlignmentX(LEFT_ALIGNMENT);

        moduleNameLabel = new JLabel();
        moduleNameLabel.setFont(moduleNameFont);
        moduleNameLabel.setFocusable(false);;

        allTopicsLabel = new JLabel();
        allTopicsLabel.setFont(allTopicsFont);
        allTopicsLabel.setForeground(UIUtil.getActiveTextColor());
        allTopicsLabel.setFocusable(false);;

        moduleNamePanel.add(moduleNameLabel);
        moduleNamePanel.add(Box.createHorizontalGlue());
        moduleNamePanel.add(allTopicsLabel);
        moduleNamePanel.setBorder(new EmptyBorder(0, 0, 0, 4));

        lessonNameLabel = new JLabel();
        lessonNameLabel.setFont(lessonNameFont);
        lessonNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        lessonNameLabel.setFocusable(false);;

//        lessonMessageContainer = new JPanel();
//        messages = new ArrayList<LessonMessage>();
//        lessonMessageContainer.setLayout(new BoxLayout(lessonMessageContainer, BoxLayout.Y_AXIS));
//        lessonMessageContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        lessonMessagePane = new LessonMessagePane();
        lessonMessagePane.setFocusable(false);
        //Set lessonMessagePane UI
        lessonMessagePane.setBackground(background);
        lessonMessagePane.setUI(defaultTextColor, shortcutTextColor, lessonCodeColor, lessonLinkColor, passedColor);
        lessonMessagePane.setAlignmentX(Component.LEFT_ALIGNMENT);
        lessonMessagePane.setMargin(new Insets(0, 0, 0, 0));
        lessonMessagePane.setBorder(new EmptyBorder(0, 0, 0, 0));
        lessonMessagePane.setMaximumSize(new Dimension(width, 10000));

        //Set Next Button UI
        lessonNextButton = new JButton("Next");
//        lessonNextButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        lessonNextButton.setVisible(false);
        lessonNextButton.setBackground(background);
        lessonNextButton.setMargin(new Insets(0, 0, 0, 0));
        lessonNextButton.setFocusable(false);;


        //Add new NextButton TEST
//        lessonNextTestButton = new JButton("Test Next");
//        lessonNextTestButton.setVisible(true);
//        lessonNextTestButton.setBackground(background);
//        lessonNextTestButton.setMargin(new Insets(0, 0, 0, 0));

        lessonPanel.add(moduleNamePanel);
        lessonPanel.add(Box.createRigidArea(new Dimension(5, lessonNameGap)));
        lessonPanel.add(lessonNameLabel);
//        lessonPanel.add(Box.createRigidArea(new Dimension(0, lessonNameGap)));
        lessonPanel.add(lessonMessagePane);
        lessonPanel.add(Box.createRigidArea(new Dimension(0, lessonNextButtonGap)));
        lessonPanel.add(lessonNextButton);
        lessonPanel.add(Box.createRigidArea(new Dimension(0, lessonNextButtonGap)));
    }


    public void setLessonName(String lessonName){
        lessonNameLabel.setText(lessonName);
        lessonNameLabel.setForeground(defaultTextColor);
        lessonNameLabel.setFocusable(false);
        this.revalidate();
        this.repaint();
    }

    public void setModuleName(String moduleName){
        moduleNameLabel.setText(moduleName);
        moduleNameLabel.setForeground(defaultTextColor);
        moduleNameLabel.setFocusable(false);
        allTopicsLabel.setText(EducationBundle.message("learn.ui.alltopics"));
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

        for (final Message message : messages) {
            if(message.getType() == Message.MessageType.LINK)  {
                //add link handler
                message.setRunnable(new Runnable() {
                    @Override
                    public void run() {
                        final Lesson lesson = CourseManager.getInstance().findLesson(message.getText());
                        if (lesson != null) {
                            try {
                                Project project = ProjectUtil.guessCurrentProject(EduPanel.this);
                                CourseManager.getInstance().openLesson(project, lesson);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }

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
        try {
            lessonMessagePane.passPreviousMessages();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void setLessonPassed(){

//        lessonNameLabel.setForeground(lessonPassedColor);
        showNextButton();
        this.repaint();
    }

    private void showNextButton() {
        lessonNextButton.setVisible(true);
        lessonPanel.revalidate();
        lessonPanel.repaint();
//        lessonNextButton.requestFocus(true); focus requesting is danger here, may interfere with windows like File Structure
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
        lessonNameLabel.setIcon(null);
        lessonMessagePane.clear();
        //remove links from lessonMessagePane
        final MouseListener[] mouseListeners = lessonMessagePane.getMouseListeners();
        for (int i = 0; i < mouseListeners.length; i++) {
            lessonMessagePane.removeMouseListener(mouseListeners[i]);
        }
//        messages.clear();
        this.revalidate();
        this.repaint();
    }

    public void setNextButtonAction(final Runnable runnable) {

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
        lessonNextButton.setAction(buttonAction);
    }

    public void hideNextButton() {
        lessonNextButton.setVisible(false);
    }

    private void initModulePanel(){
        modulePanel = new ModulePanel();
        modulePanel.setLayout(new BoxLayout(modulePanel, BoxLayout.Y_AXIS));
        modulePanel.setBackground(background);
        modulePanel.setFocusable(false);

        //define separator
        modulePanel.setBorder(new MatteBorder(1, 0, 0, 0, separatorColor));
        modulePanel.setLeftInset(16);


    }

    public ModulePanel getModulePanel(){
        return modulePanel;
    }

    public class ModulePanel extends JPanel{
        private Course myModule;
        private Lesson myLesson;
        private BidirectionalMap<Lesson, JLabel> lessonLabelMap;
        private int leftInset = 10;

        public void init(Lesson lesson){

            myLesson = lesson;
            myModule = lesson.getCourse();
            lessonLabelMap = new BidirectionalMap<Lesson, JLabel>();
            initModuleLessons(lesson);
        }

        public void setLeftInset(int inset){
            leftInset = inset;
        }

        private void initModuleLessons(final Lesson lesson){
            if(lesson == null) return;
            if(lesson.getCourse() == null) return;
            Course course = lesson.getCourse();
            final ArrayList<Lesson> myLessons = course.getLessons();

            //if course contains one lesson only
            if (myLessons.size() == 1) return;

            //create ModuleLessons region
            JLabel moduleLessons = new JLabel();
            String courseName = lesson.getCourse().getName();
            String courseNameLessons = courseName + " Lessons";
            moduleLessons.setText(courseNameLessons);
            moduleLessons.setFont(allLessonsFont);
            moduleLessons.setFocusable(false);

            add(Box.createRigidArea(new Dimension(0, 5)));
            add(moduleLessons);
            add(Box.createRigidArea(new Dimension(0, 10)));

            buildLessonLabels(lesson, myLessons);
            setMaximumSize(new Dimension(width - insets * 2, modulePanel.getPreferredSize().height));
        }

        private void buildLessonLabels(Lesson lesson, final ArrayList<Lesson> myLessons) {
            for (int i = 0; i < myLessons.size(); i++) {
                Lesson currentLesson = myLessons.get(i);
                String id = currentLesson.getName();

                final JLabel e = new JLabel(id);
                e.setHorizontalTextPosition(SwingConstants.LEFT);
                e.setBorder(new EmptyBorder(0, leftInset, lessonGap, 0));
                e.setFont(lessonsFont);
                e.setFocusable(false);

                if (lesson.equals(currentLesson)){
                    //selected lesson
                    e.setForeground(lessonActiveColor);
                } else {
                    e.setForeground(lessonInactiveColor);
                }
                final int index = i;
                e.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                e.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent mouseEvent) {
                        try {
                            Lesson lessonToOpen = myLessons.get(index);
                            Project project = ProjectUtil.guessCurrentProject(EduPanel.this);
                            CourseManager.getInstance().openLesson(project, lessonToOpen);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                lessonLabelMap.put(currentLesson, e);
                add(e);
            }
        }

        public void updateLessons(Lesson lesson){
            for (Lesson curLesson : lessonLabelMap.keySet()) {
                JLabel jLabel = lessonLabelMap.get(curLesson);
                if (lesson.equals(curLesson)){
                    jLabel.setForeground(lessonActiveColor);
                } else {
                    jLabel.setForeground(lessonInactiveColor);
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
    }

}
