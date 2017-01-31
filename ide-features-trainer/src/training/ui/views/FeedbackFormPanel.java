package training.ui.views;

import com.intellij.ide.ui.UISettings;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.containers.HashMap;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.learn.CourseManager;
import training.learn.LearnBundle;
import training.statistic.FeedbackEvent;
import training.ui.FeedbackManager;
import training.ui.LearnUIManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by karashevich on 18/07/16.
 */
public class FeedbackFormPanel extends JPanel {


    private JLabel caption;
    private MyJTextPane description;

    private static SimpleAttributeSet REGULAR = new SimpleAttributeSet();
    private static SimpleAttributeSet REGULAR_GRAY = new SimpleAttributeSet();
    private static SimpleAttributeSet PARAGRAPH_STYLE = new SimpleAttributeSet();

    private JPanel mainPanel;
    private JPanel submitFeedbackPanel;
    private LinkLabel backToModules;

    private ArrayList<RadioButtonRow> radioButtonRows;
    private Color radioButtonLabelColor;

    private JTextArea customFeedback;

    private JButton submitFeedbackButton;
    private JPanel submitFeedbackButtonPanel;
    public AsyncProcessIcon submitFeedbackAsyncProcessIcon;


    public FeedbackFormPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setFocusable(false);

        init();
        setOpaque(true);
        setBackground(getBackground());
        initMainPanel();
        add(mainPanel);
        add(Box.createVerticalGlue());
        add(submitFeedbackPanel);

        //set LearnPanel UI
        this.setPreferredSize(new Dimension(LearnUIManager.getInstance().getWidth(), 100));
        this.setBorder(LearnUIManager.getInstance().getEmptyBorder());

        revalidate();
        repaint();

    }


    private void init() {

        caption = new JLabel();
        caption.setOpaque(false);
        caption.setFont(LearnUIManager.getInstance().getModuleNameFont());

        description = new MyJTextPane(LearnUIManager.getInstance().getWidth());
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
                submitFeedbackAsyncProcessIcon.setVisible(true);
                submitFeedbackAsyncProcessIcon.resume();

                FeedbackEvent feedbackEvent = new FeedbackEvent(getFeedbackData());
                FeedbackManager.getInstance().submitFeedback(feedbackEvent);
            }
        });
        submitFeedbackButton.setOpaque(false);
        submitFeedbackButton.setText(LearnBundle.message("learn.feedback.submit.button"));


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
        submitFeedbackButtonPanel.add(submitFeedbackAsyncProcessIcon);
        submitFeedbackButtonPanel.setAlignmentX(LEFT_ALIGNMENT);

        StyleConstants.setFontFamily(REGULAR, LearnUIManager.getInstance().getPlainFont().getFamily());
        StyleConstants.setFontSize(REGULAR, LearnUIManager.getInstance().getFontSize());
        StyleConstants.setForeground(REGULAR, LearnUIManager.getInstance().getQuestionColor());

        StyleConstants.setFontFamily(REGULAR_GRAY, LearnUIManager.getInstance().getPlainFont().getFamily());
        StyleConstants.setFontSize(REGULAR_GRAY, LearnUIManager.getInstance().getFontSize());
        StyleConstants.setForeground(REGULAR_GRAY, LearnUIManager.getInstance().getDescriptionColor());

        StyleConstants.setLeftIndent(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setRightIndent(PARAGRAPH_STYLE, 0);
        StyleConstants.setSpaceAbove(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setSpaceBelow(PARAGRAPH_STYLE, 0.0f);
        StyleConstants.setLineSpacing(PARAGRAPH_STYLE, 0.0f);
    }

    @NotNull
    private Map<String, String> getFeedbackData() {

        Map<String, String> answer = new HashMap<>();
        for (int i = 0; i < radioButtonRows.size(); i++) {
            RadioButtonRow radioButtonRow = radioButtonRows.get(i);
            String rate = radioButtonRow.getRate();
            answer.put("rate-" + i, radioButtonRow.myRadioButtons.get(0).getName() + "/" + rate + "/" + radioButtonRow.myRadioButtons.get(radioButtonRow.myRadioButtons.size() - 1).getName());
        }

        try {
            Document document = customFeedback.getDocument();
            // we trim user answer if it exceeded 1000 symbols
            int max_length = Math.min(document.getLength(), 1000);
            answer.put("detailed-feedback", document.getText(0, max_length));
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
        return answer;
    }


    private void initMainPanel() {

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setFocusable(false);

        mainPanel.add(caption);
        mainPanel.add(Box.createVerticalStrut(LearnUIManager.getInstance().getAfterCaptionGap()));
        mainPanel.add(description);
        mainPanel.add(Box.createVerticalStrut(LearnUIManager.getInstance().getGroupGap()));

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
            questionBox.setBorder(new EmptyBorder(0, LearnUIManager.getInstance().getLeftIndent(), 0, 0));
            questionBox.setOpaque(false);
            questionBox.setLayout(new BoxLayout(questionBox, BoxLayout.X_AXIS));

            //rate bar
            MyJTextPane ratePane = new MyJTextPane(LearnUIManager.getInstance().getWidth());
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
            mainPanel.add(Box.createVerticalStrut(4 * LearnUIManager.getInstance().getHeaderGap()));
            mainPanel.add(radioButtonRow);
            mainPanel.add(Box.createVerticalStrut(LearnUIManager.getInstance().getRateQuestionGap()));
        }
        mainPanel.add(Box.createVerticalStrut(LearnUIManager.getInstance().getRateQuestionGap()));

        MyJTextPane customFeedbackQuestion = new MyJTextPane(LearnUIManager.getInstance().getWidth());

        customFeedbackQuestion.getDocument().insertString(0, FeedbackManager.getInstance().getCustomQuestion(), REGULAR);
        customFeedbackQuestion.setEditable(false);
        customFeedbackQuestion.setOpaque(false);
        customFeedbackQuestion.setParagraphAttributes(PARAGRAPH_STYLE, true);
        customFeedbackQuestion.setAlignmentX(Component.LEFT_ALIGNMENT);
        customFeedbackQuestion.setMargin(new Insets(0, 0, 0, 0));
        customFeedbackQuestion.setBorder(new EmptyBorder(0, 0, 0, 0));

        mainPanel.add(customFeedbackQuestion);
        mainPanel.add(Box.createVerticalStrut(2 * LearnUIManager.getInstance().getHeaderGap()));

        mainPanel.add(customFeedback);
        mainPanel.add(Box.createVerticalStrut(LearnUIManager.getInstance().getRateQuestionGap()));

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
        return new Dimension((int) mainPanel.getMinimumSize().getWidth() + (LearnUIManager.getInstance().getWestInset() + LearnUIManager.getInstance().getEastInset()),
                (int) mainPanel.getMinimumSize().getHeight() + (LearnUIManager.getInstance().getNorthInset() + LearnUIManager.getInstance().getSouthInset()));
    }


    @Override
    public Color getBackground() {
        if (!UIUtil.isUnderDarcula()) return LearnUIManager.getInstance().getBackgroundColor();
        else return UIUtil.getPanelBackground();
    }

    public Point getButtonPosition() {
        Point locationOnScreen = submitFeedbackButton.getLocationOnScreen();
        return new Point(locationOnScreen.x + submitFeedbackButton.getWidth() / 2, locationOnScreen.y);
    }

    public void setButtonActive() {
        submitFeedbackButton.setEnabled(true);
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
                myLowRateLabel.setForeground(LearnUIManager.getInstance().getDescriptionColor());
                myLowRateLabel.setOpaque(false);
                add(myLowRateLabel);
                add(Box.createHorizontalStrut(LearnUIManager.getInstance().getLabelLineGap()));
            }

            setFocusable(false);
            setAlignmentX(LEFT_ALIGNMENT);
            setBorder(new EmptyBorder(0, LearnUIManager.getInstance().getLeftIndent(), LearnUIManager.getInstance().getLabelVerticalGap() + UISettings.getInstance().FONT_SIZE, 0));
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            addRadioButtonRow(count);

            //add high rate label
            if (highRate != null) {
                add(Box.createHorizontalStrut(LearnUIManager.getInstance().getLabelLineGap()));
                myHighRateLabel = new JLabel(highRate);
                myHighRateLabel.setForeground(LearnUIManager.getInstance().getDescriptionColor());
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
            g.setFont(LearnUIManager.getInstance().getRadioButtonLabelFont());
            g.setColor(LearnUIManager.getInstance().getRadioButtonLabelColor());
            int height = g.getFontMetrics().getHeight();
            int width = g.getFontMetrics().stringWidth(label);
            Point location = jrb.getLocation();
            g.drawString(label, location.x + jrb.getBounds().height / 2 - width / 2, location.y + jrb.getBounds().height + LearnUIManager.getInstance().getLabelVerticalGap() + height / 2);
        }
    }
}