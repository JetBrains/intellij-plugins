package training.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import training.lesson.*;
import training.lesson.exceptons.BadCourseException;
import training.lesson.exceptons.BadLessonException;
import training.lesson.exceptons.LessonIsOpenedException;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 23/06/15.
 */
public class ShowEduUi extends AnAction{
    @Override
    public void actionPerformed(final AnActionEvent anActionEvent) {

        final Lesson lesson = CourseManager.getInstance().getCourseById("default").giveNotPassedLesson();
        try {
            CourseManager.getInstance().openLesson(anActionEvent.getProject(), lesson);
        } catch (BadCourseException e) {
            e.printStackTrace();
        } catch (BadLessonException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (LessonIsOpenedException e) {
            e.printStackTrace();
        }

    }


}
