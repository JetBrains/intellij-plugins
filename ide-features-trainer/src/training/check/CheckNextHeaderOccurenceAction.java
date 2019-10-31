package training.check;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

public class CheckNextHeaderOccurenceAction implements Check{


    @Override
    public void set(Project project, Editor editor) {

    }

    @Override
    public void before() {

    }

    @Override
    public boolean check() {
        return false;
    }

    @Override
    public boolean listenAllKeys() {
        return false;
    }
}
