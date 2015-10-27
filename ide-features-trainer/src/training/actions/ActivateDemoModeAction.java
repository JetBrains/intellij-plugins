package training.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import training.editor.EduEditor;

/**
 * Created by karashevich on 26/10/15.
 */
public class ActivateDemoModeAction extends ToggleAction {
    @Override
    public boolean isSelected(AnActionEvent e) {
        final Project project = e.getProject();
        final FileEditor[] selectedEditors = FileEditorManager.getInstance(project).getSelectedEditors();
        EduEditor eduEditor = null;

        for (FileEditor selectedEditor : selectedEditors) {
            if(selectedEditor instanceof EduEditor){
                eduEditor = (EduEditor) selectedEditor;
                break;
            }
        }

        if (eduEditor != null) {
            return eduEditor.isDemoModeOn();
        } else {
            return false;
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        final Project project = e.getProject();
        final FileEditor[] selectedEditors = FileEditorManager.getInstance(project).getSelectedEditors();
        EduEditor eduEditor = null;

        for (FileEditor selectedEditor : selectedEditors) {
            if(selectedEditor instanceof EduEditor){
                eduEditor = (EduEditor) selectedEditor;
                break;
            }
        }

        if (eduEditor == null) {
            e.getPresentation().setEnabled(false);
        } else {
            e.getPresentation().setEnabled(true);
        }

    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        final Project project = e.getProject();
        final FileEditor[] selectedEditors = FileEditorManager.getInstance(project).getSelectedEditors();
        EduEditor eduEditor = null;

        for (FileEditor selectedEditor : selectedEditors) {
            if(selectedEditor instanceof EduEditor){
                eduEditor = (EduEditor) selectedEditor;
                break;
            }
        }
        if (eduEditor == null) {
            state = false;
            return;
        }

        if (state) {
            try {
                eduEditor.activateDemoMode();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else {
            eduEditor.deactivateDemoMode();
        }
    }
}
