package training.solutions.EditorBasics;

import com.intellij.openapi.actionSystem.AnAction;
import training.commands.BadCommandException;
import training.commands.ExecutionList;
import training.lesson.LessonProcessor;
import training.testFramework.LessonSolution;
import training.util.PerformActionUtil;

import java.util.concurrent.ExecutionException;

public class CollapseSolution implements LessonSolution{

    @Override
    public void solveStep() throws InterruptedException, ExecutionException, BadCommandException {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;

        int stepNumber = currentExecutionList.getElements().size() - 1;
        if (stepNumber == 3) {
            final String actionName = "CollapseRegion";
            PerformActionUtil.performEditorAction(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 2){
            final String actionName = "ExpandRegion";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 1){
            final String actionName = "CollapseAllRegions";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 0){
            final String actionName = "ExpandAllRegions";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
    }
}