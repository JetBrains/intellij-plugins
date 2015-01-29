package org.jetbrains.training;

/**
 * Created by karashevich on 29/01/15.
 */
public class BadCourseException extends Exception{

    public BadCourseException() {
    }

    public BadCourseException(String s) {
        super(s);
    }
}
