package training.solutions.Navigation;

import com.intellij.openapi.editor.Editor;
import training.commands.ExecutionList;
import training.learn.LessonProcessor;
import training.solutions.BaseSolutionClass;
import training.testFramework.LessonSolution;
import training.util.PerformActionUtil;

/**
 * Created by karashevich on 28/12/15.
 */
public class JumpToSourceSolution implements LessonSolution {
    @Override
    public void solveStep() throws Exception {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        Editor editor = currentExecutionList.getEditor();

        int stepNumber = currentExecutionList.getElements().size() - 1;

        if (stepNumber == 3){
            final String actionName = "EditSource";
            PerformActionUtil.performActionDisabledPresentation(actionName, editor);
        }
        if (stepNumber == 0){
            final String actionName = "EditSource";
            BaseSolutionClass.gotoOffset(editor, 263);
            PerformActionUtil.performActionDisabledPresentation(actionName, editor);
        }
    }
}
