package training.lesson.exceptons;

import com.intellij.openapi.projectRoots.JavaSdkVersion;

/**
 * Created by karashevich on 09/09/15.
 */
public class OldJdkException extends Exception {

    public OldJdkException(String s) {
        super(s);
    }

    public OldJdkException(JavaSdkVersion javaSdkVersion) {
        super(" Old Java SDK version for Project SDK.");
    }

    public OldJdkException(JavaSdkVersion javaSdkVersion, JavaSdkVersion atLeastVersion) {
        super(" Old Java SDK version for Project SDK. Please use version " + atLeastVersion.toString());
    }
}
