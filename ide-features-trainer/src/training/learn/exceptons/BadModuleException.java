package training.learn.exceptons;

/**
 * Created by karashevich on 29/01/15.
 */
public class BadModuleException extends Exception{

    public BadModuleException() {
    }

    public BadModuleException(String s) {
        super(s);
    }
}
