package training.lesson.exceptons;

import training.lesson.EducationBundle;

/**
 * Created by karashevich on 19/10/15.
 */
public class NoSdkException extends Exception {
    public NoSdkException(){
        super(EducationBundle.message("dialog.noSdk.message"));
    }
}
