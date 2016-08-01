package training.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import training.learn.dialogs.LessonDialog;

/**
 * Created by karashevich on 15/01/16.
 */
public class OpenLessonDialog extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final LessonDialog lessonDialog = LessonDialog.createForProject(e.getProject());
        lessonDialog.setContent("blockCaret.html");
        lessonDialog.show();
    }
}
