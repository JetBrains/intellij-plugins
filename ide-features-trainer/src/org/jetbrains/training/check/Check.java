package org.jetbrains.training.check;

import com.intellij.openapi.project.Project;
import org.jetbrains.training.editor.EduEditor;

/**
 * Created by karashevich on 21/08/15.
 */
public interface Check {

    void set(Project project, EduEditor eduEditor);

    void before();

    boolean check();

}
