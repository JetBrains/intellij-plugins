package training.editor.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;

/**
 * Created by karashevich on 16/09/15.
 */
public class HideProjectTreeAction extends DumbAwareAction implements EduActions {

    final static public String PROJECT_ID = "Project";
    final static public String actionId = "EduHideProjectTreeAction";

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) return;
        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        windowManager.getToolWindow(PROJECT_ID).hide(null);
    }

    @Override
    public String getActionId() {
        return actionId;
    }

    @Override
    public void unregisterAction() {
    }
}
