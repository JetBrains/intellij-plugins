package training.lesson.exceptons;

/**
 * Created by karashevich on 29/01/15.
 */
public class LessonIsOpenedException extends Exception{

    public LessonIsOpenedException(String s) {
        super(s);
    }

    public LessonIsOpenedException() {
        super();
    }
}
