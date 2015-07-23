package org.jetbrains.training.lesson;

/**
 * Created by karashevich on 29/01/15.
 */
public class BadLessonException extends Exception{

    public BadLessonException(String s) {
        super(s);
    }

    public BadLessonException() {
        super();
    }
}
