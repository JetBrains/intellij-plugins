package training.learn;

import training.learn.exceptons.BadCourseException;
import training.learn.exceptons.BadLessonException;
import training.learn.exceptons.LessonIsOpenedException;

import java.awt.*;
import java.io.IOException;
import java.util.EventListener;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 27/02/15.
 */
public interface LessonListener extends EventListener {

    void lessonStarted(Lesson lesson);

    void lessonPassed(Lesson lesson);

    void lessonClosed(Lesson lesson);

    void lessonNext(Lesson lesson) throws BadLessonException, ExecutionException, IOException, FontFormatException, InterruptedException, BadCourseException, LessonIsOpenedException;

}