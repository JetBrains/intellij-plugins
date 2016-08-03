package training.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.learn.LearnBundle;
import training.statistic.FeedbackEvent;
import training.statistic.FeedbackSender;

import java.util.ArrayList;

/**
 * Created by karashevich on 18/07/16.
 */
public class FeedbackManager {

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

    public void submitFeedback(final FeedbackEvent feedbackEvent, final Runnable doWhenSuccess, final Runnable doWhenNotSuccess) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            if (ServiceManager.getService(FeedbackSender.class).sendStatsData(feedbackEvent.toString())) {
                doWhenSuccess.run();
            } else {
                doWhenNotSuccess.run();
            }
        });
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

}
