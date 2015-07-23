package org.jetbrains.training.lesson;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 27/02/15.
 */
public class LessonListenerAdapter implements LessonListener {

    public void lessonStarted(Lesson lesson){
    }
    @Override

    public void lessonPassed(Lesson lesson){

    }

    @Override
    public void lessonClosed(Lesson lesson){

    }

    @Override
    public void lessonNext(Lesson lesson) throws BadLessonException, ExecutionException, IOException, FontFormatException, InterruptedException, BadCourseException, LessonIsOpenedException {
    }

}