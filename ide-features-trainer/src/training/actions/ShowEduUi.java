package training.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import training.learn.*;
import training.learn.exceptons.BadModuleException;
import training.learn.exceptons.BadLessonException;
import training.learn.exceptons.LessonIsOpenedException;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 23/06/15.
 */
public class ShowEduUi extends AnAction{
    @Override
    public void actionPerformed(final AnActionEvent anActionEvent) {

        final Lesson lesson = CourseManager.getInstance().getModuleById("default").giveNotPassedLesson();
        try {
            CourseManager.getInstance().openLesson(anActionEvent.getProject(), lesson);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
