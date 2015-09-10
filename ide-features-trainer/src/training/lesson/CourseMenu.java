package training.lesson;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by karashevich on 21/07/15.
 */
public class CourseMenu extends ActionGroup {

    @NotNull
    @Override
    public AnAction[] getChildren(AnActionEvent anActionEvent) {
        AnAction[] actions = new AnAction[CourseManager.getInstance().courses.size()];
        actions = CourseManager.getInstance().courses.toArray(actions);
        return actions;
    }
}
