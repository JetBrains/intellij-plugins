package training.learn.exceptons;

import training.learn.LearnBundle;

/**
 * Created by karashevich on 19/10/15.
 */
public class NoSdkException extends Exception {
    public NoSdkException(){
        super(LearnBundle.message("dialog.noSdk.message"));
    }
}
