package training.solutions.Navigation;

import training.commands.ExecutionList;
import training.lesson.LessonProcessor;
import training.solutions.BaseSolutionClass;
import training.testFramework.LessonSolution;
import training.util.PerformActionUtil;

/**
 * Created by karashevich on 28/12/15.
 */
public class FileStructureSolution implements LessonSolution {
    @Override
    public void solveStep() throws Exception {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;

        int stepNumber = currentExecutionList.getElements().size() - 1;

        if (stepNumber == 0){
            final String actionName = "FileStructurePopup";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
    }
}
