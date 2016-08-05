package training.ui;

import com.intellij.ide.ui.UISettings;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.learn.CourseManager;
import training.learn.LearnBundle;
import training.statistic.FeedbackEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Created by karashevich on 18/07/16.
 */
public class FeedbackFormPanel extends JPanel {

    private int left_indent;
    private int width;

    //UI Preferences
    private int north_inset;
    private int west_inset;
    private int south_inset;
    private int east_inset;

    private JLabel caption;
    private MyJTextPane description;

    private static SimpleAttributeSet REGULAR = new SimpleAttributeSet();
    private static SimpleAttributeSet REGULAR_GRAY = new SimpleAttributeSet();
    private static SimpleAttributeSet PARAGRAPH_STYLE = new SimpleAttributeSet();
    private int headerGap;
    private int afterCaptionGap;
    private int rateQuestionGap;
    private int groupGap;

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
    private int label_line_gap = 12;
    private int label_vertical_gap = 5;

    private JTextArea customFeedback;

    private JButton submitFeedbackButton;
    private JPanel submitFeedbackButtonPanel;
    private JLabel submitFeedbackStatusLabel;
    private AsyncProcessIcon submitFeedbackAsyncProcessIcon;


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

        left_indent = 17;

        //UI colors and fonts
        fontSize = UISettings.getInstance().FONT_SIZE;
        questionFont = new Font(UISettings.getInstance().FONT_FACE, Font.PLAIN, fontSize);
        questionColor = new JBColor(new Color(0, 0, 0), Gray._202);
        descriptionFont = new Font(UISettings.getInstance().FONT_FACE, Font.PLAIN, fontSize);
        descriptionColor = Gray._128;
        radioButtonLabelFont = new Font(UISettings.getInstance().FONT_FACE, Font.PLAIN, fontSize - 2);
        radioButtonLabelColor = Gray._128;

        caption = new JLabel();
        caption.setOpaque(false);
        caption.setFont(new Font(UISettings.getInstance().FONT_FACE, Font.BOLD, fontSize + 1));

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

        submitFeedbackButton = new JButton();
        submitFeedbackButton.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitFeedbackButton.setEnabled(false);
                submitFeedbackStatusLabel.setText("feedback is sending");
                submitFeedbackAsyncProcessIcon.setVisible(true);
                submitFeedbackAsyncProcessIcon.resume();

                FeedbackEvent feedbackEvent = new FeedbackEvent(getFeedbackData());
                FeedbackManager.getInstance().submitFeedback(feedbackEvent, () -> {
                    //success
                    submitFeedbackAsyncProcessIcon.suspend();
                    submitFeedbackAsyncProcessIcon.setVisible(false);
                    submitFeedbackStatusLabel.setText("feedback is sent.");
                }, () -> {
                    //not success
                    submitFeedbackAsyncProcessIcon.suspend();
                    submitFeedbackAsyncProcessIcon.setVisible(false);
                    submitFeedbackStatusLabel.setText("sending error. Please try later.");
                });
            }
        });
        submitFeedbackButton.setOpaque(false);
        submitFeedbackButton.setText(LearnBundle.message("learn.feedback.submit.button"));

        submitFeedbackStatusLabel = new JLabel();
        submitFeedbackStatusLabel.setOpaque(false);

        submitFeedbackAsyncProcessIcon = new AsyncProcessIcon("Progress") {
            @Override
            protected void paintIcon(Graphics g, Icon icon, int x, int y) {
                super.paintIcon(g, icon, x, y);
            }
        };
        submitFeedbackAsyncProcessIcon.setVisible(false);

        submitFeedbackButtonPanel = new JPanel();
        submitFeedbackButtonPanel.setLayout(new BoxLayout(submitFeedbackButtonPanel, BoxLayout.LINE_AXIS));
        submitFeedbackButtonPanel.setOpaque(false);

        submitFeedbackButtonPanel.add(submitFeedbackButton);
        submitFeedbackButtonPanel.add(Box.createHorizontalStrut(12));
        submitFeedbackButtonPanel.add(submitFeedbackStatusLabel);
        submitFeedbackButtonPanel.add(Box.createHorizontalStrut(6));
        submitFeedbackButtonPanel.add(submitFeedbackAsyncProcessIcon);
        submitFeedbackButtonPanel.setAlignmentX(LEFT_ALIGNMENT);


        headerGap = 2;
        afterCaptionGap = 12;
        rateQuestionGap = 16;
        groupGap = 24;

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

    @NotNull
    private String getFeedbackData() {
        StringBuilder answer = new StringBuilder();
        for (RadioButtonRow radioButtonRow : radioButtonRows) {
            String myQuestion = radioButtonRow.myQuestion;
            String rate = radioButtonRow.getRate();
            String myLowRate = radioButtonRow.myLowRate;
            String myMaxRate = radioButtonRow.myMaxRate;
            answer.
                    append("question=\"" + myQuestion + "\" ").
                    append("rate=\"" + myLowRate + "/" + rate + "/" + myMaxRate + "\"");
        }
        try {
            Document document = customFeedback.getDocument();
            // we trim user answer if it exceeded 1000 symbols
            int max_length = Math.min(document.getLength(), 1000);
            answer.append("detailed-feedback=\"" + document.getText(0, max_length) + "\"");
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
        return answer.toString();
    }


    private void initMainPanel() {


        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setFocusable(false);

        mainPanel.add(caption);
        mainPanel.add(Box.createVerticalStrut(afterCaptionGap));
        mainPanel.add(description);
        mainPanel.add(Box.createVerticalStrut(groupGap));

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
        backToModules = new LinkLabel(LearnBundle.message("learn.feedback.back.to.modules"), null);

        backToModules.setListener((linkLabel, o) -> {
            //return to modules view
            CourseManager.getInstance().setModulesView();
        }, null);
        submitFeedbackPanel.add(Box.createHorizontalGlue());
        submitFeedbackPanel.add(backToModules);


        caption.setText(LearnBundle.message("learn.feedback.caption"));
        try {
            description.getDocument().insertString(0, FeedbackManager.getInstance().getDescription(), REGULAR);
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
            questionBox.setBorder(new EmptyBorder(0, left_indent, 0, 0));
            questionBox.setOpaque(false);
            questionBox.setLayout(new BoxLayout(questionBox, BoxLayout.X_AXIS));

            //rate bar
            MyJTextPane ratePane = new MyJTextPane(width);
            ratePane.getDocument().insertString(0, rateQuestion.question, REGULAR);
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
            mainPanel.add(Box.createVerticalStrut(4 * headerGap));
            mainPanel.add(radioButtonRow);
            mainPanel.add(Box.createVerticalStrut(rateQuestionGap));
        }
        mainPanel.add(Box.createVerticalStrut(rateQuestionGap));

        MyJTextPane customFeedbackQuestion = new MyJTextPane(width);

        customFeedbackQuestion.getDocument().insertString(0, FeedbackManager.getInstance().getCustomQuestion(), REGULAR);
        customFeedbackQuestion.setEditable(false);
        customFeedbackQuestion.setOpaque(false);
        customFeedbackQuestion.setParagraphAttributes(PARAGRAPH_STYLE, true);
        customFeedbackQuestion.setAlignmentX(Component.LEFT_ALIGNMENT);
        customFeedbackQuestion.setMargin(new Insets(0, 0, 0, 0));
        customFeedbackQuestion.setBorder(new EmptyBorder(0, 0, 0, 0));

        mainPanel.add(customFeedbackQuestion);
        mainPanel.add(Box.createVerticalStrut(2 * headerGap));

        mainPanel.add(customFeedback);
        mainPanel.add(Box.createVerticalStrut(rateQuestionGap));

        mainPanel.add(submitFeedbackButtonPanel);
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

        JLabel myLowRateLabel;
        JLabel myHighRateLabel;

        RadioButtonRow(String questionRate, int count, @Nullable String lowRate, @Nullable String highRate) {
            super();
            myCount = count;
            myQuestion = questionRate;
            myRadioButtons = new ArrayList<>(myCount);

            myLowRate = lowRate;
            myMaxRate = highRate;

            //add low rate label
            if (lowRate != null) {
                myLowRateLabel = new JLabel(lowRate);
                myLowRateLabel.setForeground(descriptionColor);
                myLowRateLabel.setOpaque(false);
                add(myLowRateLabel);
                add(Box.createHorizontalStrut(label_line_gap));
            }

            setFocusable(false);
            setAlignmentX(LEFT_ALIGNMENT);
            setBorder(new EmptyBorder(0, left_indent, label_vertical_gap + UISettings.getInstance().FONT_SIZE, 0));
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            addRadioButtonRow(count);

            //add high rate label
            if (highRate != null) {
                add(Box.createHorizontalStrut(label_line_gap));
                myHighRateLabel = new JLabel(highRate);
                myHighRateLabel.setForeground(descriptionColor);
                myHighRateLabel.setOpaque(false);
                add(myHighRateLabel);
            }
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
                jrb.setName(Integer.toString(i));
                jrb.setOpaque(false);
                //eliminate insets, borders...
                jrb.setBorder(null);
                btngrp.add(jrb);
                add(jrb);
                myRadioButtons.add(jrb);
            }
        }

        @NotNull
        public String getRate() {
            for (JRadioButton myRadioButton : myRadioButtons) {
                if (myRadioButton.isSelected()) return myRadioButton.getName();
            }
            return "missed";
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (myRadioButtons != null) {
                for (int i = 0; i < myRadioButtons.size(); i++) {
                    paintLabel(g, Integer.toString(i + 1), myRadioButtons.get(i));
                }
            }
        }

        private void paintLabel(Graphics g, String label, JRadioButton jrb) {
            FontMetrics fontMetrics = g.getFontMetrics();
            g.setFont(radioButtonLabelFont);
            g.setColor(radioButtonLabelColor);
            int height = g.getFontMetrics().getHeight();
            int width = g.getFontMetrics().stringWidth(label);
            Point location = jrb.getLocation();
            g.drawString(label, location.x + jrb.getBounds().height / 2 - width / 2, location.y + jrb.getBounds().height + label_vertical_gap + height / 2);
        }

//        @Override
//        public Dimension getPreferredSize() {
//            Dimension preferredSize = super.getPreferredSize();
//            Graphics g = getGraphics();
//            FontMetrics fontMetrics = g.getFontMetrics();
//            g.setFont(radioButtonLabelFont);
//            g.setColor(radioButtonLabelColor);
//            int height = g.getFontMetrics().getHeight();
//
//            return new Dimension(preferredSize.width, preferredSize.height + label_vertical_gap + 2 * height);
//        }
    }
}