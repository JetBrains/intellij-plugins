package training.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.learn.CourseManager;
import training.learn.LearnBundle;
import training.statistic.FeedbackEvent;
import training.statistic.FeedbackSender;

import java.awt.*;
import java.util.ArrayList;

import static training.util.PerformActionUtil.performAction;

/**
 * Created by karashevich on 18/07/16.
 */
public class FeedbackManager {

    private FeedbackFormPanel myFeedbackFormPanel;

    private ArrayList<RateQuestion> rateQuestions;
    private String customQuestion;
    private String description;

    public static FeedbackManager getInstance() {
        return ServiceManager.getService(FeedbackManager.class);
    }

    FeedbackManager() {
        rateQuestions = new ArrayList<>();
        initRateQuestions();
    }

    private void initRateQuestions(){
        rateQuestions.add(new RateQuestion(
                LearnBundle.message("learn.feedback.rate.question.1"),
                LearnBundle.message("learn.feedback.rate.question.1.low"),
                LearnBundle.message("learn.feedback.rate.question.1.high")));
        rateQuestions.add(new RateQuestion(
                LearnBundle.message("learn.feedback.rate.question.2"),
                LearnBundle.message("learn.feedback.rate.question.2.low"),
                LearnBundle.message("learn.feedback.rate.question.2.high")));
        rateQuestions.add(new RateQuestion(
                LearnBundle.message("learn.feedback.rate.question.3"),
                LearnBundle.message("learn.feedback.rate.question.3.low"),
                LearnBundle.message("learn.feedback.rate.question.3.high")));
        customQuestion = LearnBundle.message("learn.feedback.detailed.feedback");
        description = LearnBundle.message("learn.feedback.description");
    }

    public ArrayList<RateQuestion> getRateQuestions() {
        return rateQuestions;
    }

    public String getCustomQuestion() {
        return customQuestion;
    }

    public String getDescription() {
        return description;
    }

    public void submitFeedback(final FeedbackEvent feedbackEvent) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            if (ServiceManager.getService(FeedbackSender.class).sendStatsData(feedbackEvent.toString()))
                UIUtil.invokeLaterIfNeeded(() -> doWhenSuccess());
            else
                UIUtil.invokeLaterIfNeeded(() -> doWhenNotSuccess());

        });
    }

    public FeedbackFormPanel getFeedbackFormPanel() {
        if (myFeedbackFormPanel == null) myFeedbackFormPanel = new FeedbackFormPanel();
        return myFeedbackFormPanel;
    }

    class RateQuestion{
        String question;
        String lowRate;
        String maxRate;

        RateQuestion(@NotNull String question, @Nullable String lowRate, @Nullable String maxRate){
            this.question = question;
            this.lowRate = lowRate;
            this.maxRate = maxRate;
        }

        RateQuestion(@NotNull String question) {
            this.question = question;
            lowRate = null;
            maxRate = null;
        }
    }

    public void doWhenSuccess() {
        if (myFeedbackFormPanel.submitFeedbackAsyncProcessIcon != null) {
            myFeedbackFormPanel.submitFeedbackAsyncProcessIcon.suspend();
            myFeedbackFormPanel.submitFeedbackAsyncProcessIcon.setVisible(false);
        }
        CourseManager.getInstance().setModulesView();
        try {
            showBalloon(CourseManager.getInstance().getMainLearnPanel().getSendFeedbackPosition(), LearnBundle.message("learn.feedback.submit.success"), MessageType.WARNING.getPopupBackground());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doWhenNotSuccess() {
        if (myFeedbackFormPanel.submitFeedbackAsyncProcessIcon != null) {
            myFeedbackFormPanel.submitFeedbackAsyncProcessIcon.suspend();
            myFeedbackFormPanel.submitFeedbackAsyncProcessIcon.setVisible(false);
            myFeedbackFormPanel.setButtonActive();
        }
        try {
            showBalloon(myFeedbackFormPanel.getButtonPosition(), LearnBundle.message("learn.feedback.submit.notsuccess"), MessageType.ERROR.getPopupBackground());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void showBalloon(Point point, String text, Color popupBackground) throws InterruptedException {

        String balloonText = text;

        BalloonBuilder builder =
                JBPopupFactory.getInstance().
                        createHtmlTextBalloonBuilder(balloonText, null, popupBackground, null)
                        .setHideOnClickOutside(true)
                        .setCloseButtonEnabled(false)
                        .setHideOnKeyOutside(true)
                        .setFadeoutTime(3000);
        final Balloon myBalloon = builder.createBalloon();

        myBalloon.show(new RelativePoint(point), Balloon.Position.above);

    }
}
