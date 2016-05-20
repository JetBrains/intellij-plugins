package training.solutions.Refactorings;

import training.commands.ExecutionList;
import training.learn.LessonProcessor;
import training.testFramework.LessonSolution;
import training.util.PerformActionUtil;

/**
 * Created by karashevich on 23/12/15.
 */
public class RenameSolution implements LessonSolution {

    @Override
    public void solveStep() throws Exception {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;

        int stepNumber = currentExecutionList.getElements().size() - 1;

        if (stepNumber == 4){
            final String actionName = "RenameElement";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 3){
            final String actionName = "NextTemplateVariable";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 1){
            final String actionName = "RenameElement";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 0){
            final String actionName = "NextTemplateVariable";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }

    }
}
