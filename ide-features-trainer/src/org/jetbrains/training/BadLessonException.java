package org.jetbrains.training;

/**
 * Created by karashevich on 29/01/15.
 */
public class BadLessonException extends Exception{

    public BadLessonException(String s) {
        super("BadLessonException:" + s);
    }

    public BadLessonException() {
        super();
    }
}
