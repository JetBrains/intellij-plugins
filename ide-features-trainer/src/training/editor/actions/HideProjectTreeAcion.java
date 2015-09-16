package training.editor.actions;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.ProjectViewImpl;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.testFramework.ProjectViewTestUtil;
import com.intellij.ui.content.Content;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Created by karashevich on 16/09/15.
 */
public class HideProjectTreeAcion extends DumbAwareAction implements EduActions {

    final static public String PROJECT_ID = "Project";


    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) return;
        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        windowManager.getToolWindow(PROJECT_ID).hide(null);
    }

    @Override
    public void unregisterAction() {
    }
}
