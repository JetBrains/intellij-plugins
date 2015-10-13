package training.lesson;

import com.intellij.openapi.util.Pair;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by karashevich on 13/10/15.
 */
public class LessonLog {

    final private Lesson myLesson;
    private ArrayList<Pair<Date, String>> logData;
    public int exerciseCount = 0;

    public LessonLog(Lesson lesson) {
        myLesson = lesson;
        logData = new ArrayList<Pair<Date, String>>();
        log("Log is created. Lesson:" + lesson.getName());
    }

    public void log(String actionString){
        logData.add(Pair.create(new Date(), actionString));
    }

    public void print(){
        for (Pair<Date, String> dateStringPair : logData) {
            System.out.println(dateStringPair.first + ": " + dateStringPair.first);
        }
    }

    public String exportToString(){
        StringBuilder sb = new StringBuilder();
        for (Pair<Date, String> dateStringPair : logData) {
            sb.append(dateStringPair.first).append(": ").append(dateStringPair.first).append("; ");
        }
        return sb.toString();
    }
}
