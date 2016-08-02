package training.ui;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;
import training.learn.CourseManager;
import training.learn.LearnBundle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by karashevich on 18/07/16.
 */
public class FeedbackFormPanel extends JPanel {

    private int width;

    //UI Preferences
    private int north_inset;
    private int west_inset;
    private int south_inset;
    private int east_inset;
    private int check_width;
    private int check_right_indent;

    private JLabel caption;
    private MyJTextPane description;

    private static SimpleAttributeSet REGULAR = new SimpleAttributeSet();
    private static SimpleAttributeSet REGULAR_GRAY = new SimpleAttributeSet();
    private static SimpleAttributeSet PARAGRAPH_STYLE = new SimpleAttributeSet();
    private int headerGap;
    private int afterCaptionGap;
    private int moduleGap;
    private Font questionRateLabelFont;
    private Font questionFont;
    private Color questionColor;
    private int fontSize;

    private Font descriptionFont;
    private Color descriptionColor;

    private JPanel mainPanel;
    private JPanel submitFeedbackPanel;
    private LinkLabel backToModules;

    private ArrayList<RadioButtonRow> radioButtonRows;
    private Font radioButtonLabelFont;
    private Color radioButtonLabelColor;


    private JTextArea customFeedback;

    private JButton submitFeedbackButton;

    public FeedbackFormPanel(int width) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setFocusable(false);
        this.width = width;

        //Obligatory block
        generalizeUI();
        setOpaque(true);
        setBackground(getBackground());
        initMainPanel();
        add(mainPanel);
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
        check_width = LearnIcons.CheckmarkGray.getIconWidth();
        check_right_indent = 5;

        //UI colors and fonts
        fontSize = 12;
        questionRateLabelFont = new Font(UIUtil.getLabelFont().getName(), Font.BOLD, fontSize + 2);
        questionFont = new Font(UIUtil.getLabelFont().getName(), Font.PLAIN, fontSize + 1);
        questionColor = new JBColor(new Color(0, 0, 0), Gray._202);
        descriptionFont = new Font(UIUtil.getLabelFont().getName(), Font.PLAIN, fontSize + 1);
        descriptionColor = Gray._128;
        radioButtonLabelFont = new Font(UIUtil.getLabelFont().getName(), Font.PLAIN, fontSize - 2);
        radioButtonLabelColor = Gray._128;

        caption = new JLabel();
        caption.setOpaque(false);
        caption.setFont(new Font(UIUtil.getLabelFont().getName(), Font.BOLD, fontSize + 2));

        description = new MyJTextPane(width);
        description.setOpaque(false);
        description.setEditable(false);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        description.setMargin(new Insets(0, 0, 0, 0));
        description.setBorder(new EmptyBorder(0, 0, 0, 0));


        customFeedback = new JTextArea();
        customFeedback.setEditable(true);
        customFeedback.setOpaque(true);
        customFeedback.setAlignmentX(Component.LEFT_ALIGNMENT);
        customFeedback.setMargin(new Insets(10, 0, 0, 0));
        customFeedback.setBorder(new EmptyBorder(0, 0, 0, 0));


        submitFeedbackButton = new JButton(LearnBundle.message("learn.ui.feedback.submit.button"));
        submitFeedbackButton.setOpaque(false);

        headerGap = 2;
        afterCaptionGap = 12;
        moduleGap = 20;

        StyleConstants.setFontFamily(REGULAR, questionFont.getFamily());
        StyleConstants.setFontSize(REGULAR, questionFont.getSize());
        StyleConstants.setForeground(REGULAR, questionColor);

        StyleConstants.setFontFamily(REGULAR_GRAY, descriptionFont.getFamily());
        StyleConstants.setFontSize(REGULAR_GRAY, descriptionFont.getSize());
        StyleConstants.setForeground(REGULAR_GRAY, descriptionColor);

        StyleConstants.setLeftIndent(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setRightIndent(PARAGRAPH_STYLE, 0);
        StyleConstants.setSpaceAbove(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setSpaceBelow(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setLineSpacing(PARAGRAPH_STYLE, 0.0f);
    }


    private void initMainPanel() {


        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setFocusable(false);

        mainPanel.add(caption);
        mainPanel.add(Box.createVerticalStrut(afterCaptionGap));
        mainPanel.add(description);
        mainPanel.add(Box.createVerticalStrut(moduleGap));

        try {
            initRateQuestionsPanel();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        //add JTextField for a custom feedback


        submitFeedbackPanel = new JPanel();
        submitFeedbackPanel.setLayout(new BoxLayout(submitFeedbackPanel, BoxLayout.LINE_AXIS));
        submitFeedbackPanel.setOpaque(false);
        submitFeedbackPanel.setAlignmentX(LEFT_ALIGNMENT);
        backToModules = new LinkLabel(LearnBundle.message("learn.ui.feedback.back.to.modules"), null);

        backToModules.setListener((linkLabel, o) -> {
            //return to modules view
            CourseManager.getInstance().setModulesView();
        }, null);
        submitFeedbackPanel.add(Box.createHorizontalGlue());
        submitFeedbackPanel.add(backToModules);


        caption.setText("Feedback Form");
        try {
            description.getDocument().insertString(0, FeedbackManager.getInstance().getDescription(), REGULAR_GRAY);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

    }

    private void initRateQuestionsPanel() throws BadLocationException {


        ArrayList<FeedbackManager.RateQuestion> rateQuestions = FeedbackManager.getInstance().getRateQuestions();
        radioButtonRows = new ArrayList<>(rateQuestions.size());

        for (FeedbackManager.RateQuestion rateQuestion : rateQuestions) {
            JPanel questionBox = new JPanel();
            questionBox.setFocusable(false);
            questionBox.setAlignmentX(LEFT_ALIGNMENT);
            questionBox.setBorder(new EmptyBorder(0, check_width + check_right_indent, 0, 0));
            questionBox.setOpaque(false);
            questionBox.setLayout(new BoxLayout(questionBox, BoxLayout.X_AXIS));

//            JLabel questionRateName = new JLabel(question);
//            questionRateName.setFont(questionRateLabelFont);
//            questionRateName.setAlignmentY(BOTTOM_ALIGNMENT);
//            questionRateName.setAlignmentX(LEFT_ALIGNMENT);
//
//            questionBox.add(questionRateName);

            //rate bar
            MyJTextPane ratePane = new MyJTextPane(width);

            Position endPosition = ratePane.getDocument().getEndPosition();
            ratePane.getDocument().insertString(endPosition.getOffset(), rateQuestion.question + "\n", REGULAR);
            ratePane.setEditable(false);
            ratePane.setOpaque(false);
            ratePane.setParagraphAttributes(PARAGRAPH_STYLE, true);

            //add radio buttons
            RadioButtonRow radioButtonRow = new RadioButtonRow(rateQuestion.question, 6, rateQuestion.lowRate, rateQuestion.maxRate);
            radioButtonRows.add(radioButtonRow);

            ratePane.setAlignmentX(Component.LEFT_ALIGNMENT);
            ratePane.setMargin(new Insets(0, 0, 0, 0));
            ratePane.setBorder(new EmptyBorder(0, 0, 0, 0));

            mainPanel.add(ratePane);
            mainPanel.add(Box.createVerticalStrut(headerGap));
            mainPanel.add(radioButtonRow);
            mainPanel.add(Box.createVerticalStrut(moduleGap));
        }

        MyJTextPane customFeedbackQuestion = new MyJTextPane(width);

        customFeedbackQuestion.getDocument().insertString(0, FeedbackManager.getInstance().getCustomQuestion(), REGULAR);
        customFeedbackQuestion.setEditable(false);
        customFeedbackQuestion.setOpaque(false);
        customFeedbackQuestion.setParagraphAttributes(PARAGRAPH_STYLE, true);
        customFeedbackQuestion.setAlignmentX(Component.LEFT_ALIGNMENT);
        customFeedbackQuestion.setMargin(new Insets(0, 0, 0, 0));
        customFeedbackQuestion.setBorder(new EmptyBorder(0, 0, 0, 0));

        mainPanel.add(customFeedbackQuestion);
        mainPanel.add(Box.createVerticalStrut(4 * headerGap));

        mainPanel.add(customFeedback);
        mainPanel.add(Box.createVerticalStrut(moduleGap));

        mainPanel.add(submitFeedbackButton);
        mainPanel.add(Box.createVerticalGlue());
    }


    public void updateMainPanel() {
        mainPanel.removeAll();
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
        return new Dimension((int) mainPanel.getMinimumSize().getWidth() + (west_inset + east_inset),
                (int) mainPanel.getMinimumSize().getHeight() + (north_inset + south_inset));
    }


    @Override
    public Color getBackground() {
        if (!UIUtil.isUnderDarcula()) return new Color(245, 245, 245);
        else return UIUtil.getPanelBackground();
    }


    private class RadioButtonRow extends JPanel {

        int myCount;
        String myQuestion;
        ArrayList<JRadioButton> myRadioButtons;
        String myLowRate;
        String myMaxRate;

        RadioButtonRow(String questionRate, int count, @Nullable String lowRate, @Nullable String maxRate) {
            super();
            myCount = count;
            myQuestion = questionRate;
            myRadioButtons = new ArrayList<>(myCount);

            myLowRate = lowRate;
            myMaxRate = maxRate;

            setFocusable(false);
            setAlignmentX(LEFT_ALIGNMENT);
            setBorder(new EmptyBorder(0, check_width + check_right_indent, 0, 0));
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            addRadioButtonRow(count);
            revalidate();
            repaint();
        }

        private void addRadioButtonRow(int count) {
            ButtonGroup btngrp = new ButtonGroup();
            for (int i = 1; i < count; i++) {
                JRadioButton jrb;
                if (i == 0 || myLowRate != null) {
                    jrb = new JRadioButton();
                } else if (i == count - 1 || myMaxRate != null) {
                    jrb = new JRadioButton();
                } else {
                    jrb = new JRadioButton();
                }
                jrb.setText(Integer.toString(i));
                jrb.setVerticalAlignment(JRadioButton.BOTTOM);
                jrb.setHorizontalAlignment(JRadioButton.CENTER);
                jrb.setOpaque(false);
                jrb.updateUI();
                jrb.setAlignmentY(TOP_ALIGNMENT);
                btngrp.add(jrb);
                add(jrb);
                myRadioButtons.add(jrb);
            }
        }

        @Nullable
        public String getRate() {
            for (JRadioButton myRadioButton : myRadioButtons) {
                if (myRadioButton.isSelected()) return myRadioButton.getText();
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (myRadioButtons != null) {
                paintLabel(g, myLowRate, myRadioButtons.get(0));
                paintLabel(g, myMaxRate, myRadioButtons.get(myRadioButtons.size() - 1));
            }
        }

        private void paintLabel(Graphics g, String label, JRadioButton jrb) {
            FontMetrics fontMetrics = g.getFontMetrics();
            g.setFont(radioButtonLabelFont);
            g.setColor(radioButtonLabelColor);
            int width = g.getFontMetrics().stringWidth(label);
            Point location = jrb.getLocation();
            g.drawString(label, jrb.getInsets().left + location.x + (int) jrb.getBounds().getHeight() / 2 - width / 2, (int) jrb.getBounds().getHeight() + 12);
            g.setFont(fontMetrics.getFont());
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension preferredSize = super.getPreferredSize();
            return new Dimension((int) preferredSize.getWidth(), (int) preferredSize.getHeight() + 18);
        }
    }
}