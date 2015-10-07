package training.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import training.lesson.Course;
import training.lesson.CourseManager;
import training.lesson.Lesson;
import training.lesson.LessonProcessor;

import java.util.ArrayList;

/**
 * Created by karashevich on 07/10/15.
 */
public class ExportAllLessonsToFile extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        final Course[] courses = CourseManager.getInstance().getCourses();
        for (Course course : courses) {
            System.out.println("\n\n##############################");
            System.out.println(course.getName().toUpperCase());
            System.out.println("##############################");
            final ArrayList<Lesson> lessons = course.getLessons();
            for (Lesson lesson : lessons) {
                System.out.println("\n" + lesson.getName());
                System.out.println(takeDescription(lesson));
            }
        }
    }

    private String takeDescription(Lesson lesson) {
        return LessonProcessor.takeDescriptionsOnly(lesson);
    }
}
