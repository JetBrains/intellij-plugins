package training.lesson;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.project.DumbModePermission;
import org.jetbrains.annotations.NotNull;
import training.actions.IndexingWarningDummyAction;

/**
 * Created by karashevich on 21/07/15.
 */
public class CourseMenu extends ActionGroup {

    @NotNull
    @Override
    public AnAction[] getChildren(AnActionEvent anActionEvent) {

        if (anActionEvent != null &&  anActionEvent.getProject() != null && !ActionUtil.isDumbMode(anActionEvent.getProject())) {
            AnAction[] actions = CourseManager.getInstance().getCourses();
            return actions;
        } else {
            AnAction[] actions =  new AnAction[1];
            actions[0] = new IndexingWarningDummyAction();
            return actions;
        }
    }
}
