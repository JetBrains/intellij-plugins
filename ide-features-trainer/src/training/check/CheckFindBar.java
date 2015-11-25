package training.check;

import com.intellij.openapi.project.Project;
import training.editor.EduEditor;

/**
 * Created by karashevich on 24/11/15.
 */
public class CheckFindBar implements Check{


    Project project;
    EduEditor eduEditor;

    @Override
    public void set(Project project, EduEditor eduEditor) {
        this.project = project;
        this.eduEditor = eduEditor;
    }

    @Override
    public void before() {

    }

    @Override
    public boolean check() {
        return (eduEditor.getEditor().getHeaderComponent() == null);
    }
}
