package training.check;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

public interface Check {

    void set(Project project, Editor editor);

    void before();

    boolean check();

    boolean listenAllKeys();

}
