package training.solutions.EditorBasics;

import com.intellij.openapi.actionSystem.AnAction;
import training.commands.BadCommandException;
import training.commands.ExecutionList;
import training.lesson.LessonProcessor;
import training.testFramework.LessonSolution;
import training.util.PerformActionUtil;

import java.util.concurrent.ExecutionException;

public class MoveSolution implements LessonSolution{

    @Override
    public void solveStep() throws InterruptedException, ExecutionException, BadCommandException {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;

        int stepNumber = currentExecutionList.getElements().size() - 1;
        if (stepNumber == 5){
            final String actionName = "MoveLineDown";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 4){
            final String actionName = "MoveLineUp";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 2){
            final String actionName = "MoveStatementUp";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
    }
}