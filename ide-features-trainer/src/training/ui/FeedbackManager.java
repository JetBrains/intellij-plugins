package training.ui;

import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        rateQuestions.add(new RateQuestion("Please rate how your training experience was:", "don't like it", "love it"));
        rateQuestions.add(new RateQuestion("Please rate how much you've learned when using the plugin:", "nothing", "everything"));
        rateQuestions.add(new RateQuestion("How likely is it that you would recommend the training plugin to a friend or colleague?", "no, thanks", "sure"));
        customQuestion = "Your detailed feedback:";
        description = "Thank you for using this plugin. We hope that our knowledge about productivity could be shared easily. Please fill this form to tell us how the experience of using this plugin was for you";
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
