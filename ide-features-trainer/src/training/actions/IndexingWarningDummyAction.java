package training.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import training.lesson.EducationBundle;

/**
 * Created by karashevich on 11/01/16.
 */
public class IndexingWarningDummyAction extends AnAction {

    public IndexingWarningDummyAction() {
        super(EducationBundle.message("action.IndexingWarningDummyAction.description"));
        this.getTemplatePresentation().setEnabled(false);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        //do nothing
    }

}
