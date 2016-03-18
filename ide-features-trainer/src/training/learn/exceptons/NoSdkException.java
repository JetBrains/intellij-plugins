package training.learn.exceptons;

import training.learn.EducationBundle;

/**
 * Created by karashevich on 19/10/15.
 */
public class NoSdkException extends Exception {
    public NoSdkException(){
        super(EducationBundle.message("dialog.noSdk.message"));
    }
}
