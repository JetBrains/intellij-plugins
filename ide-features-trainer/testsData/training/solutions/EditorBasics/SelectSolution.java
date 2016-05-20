package training.solutions.EditorBasics;

import com.intellij.openapi.actionSystem.AnAction;
import training.commands.*;
import training.learn.LessonProcessor;
import training.testFramework.LessonSolution;
import training.util.PerformActionUtil;

import java.util.concurrent.ExecutionException;

public class SelectSolution implements LessonSolution{

    @Override
    public void solveStep() throws InterruptedException, ExecutionException, BadCommandException {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;

        int stepNumber = currentExecutionList.getElements().size() - 1;
        if (stepNumber == 4) {
            final String actionName = "EditorNextWordWithSelection";
            final AnAction action = com.intellij.openapi.actionSystem.ActionManager.getInstance().getAction(actionName);
            PerformActionUtil.performEditorAction(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 3){
            final String actionName = "EditorSelectWord";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 1){
            final String actionName = "EditorUnSelectWord";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 0){
            final String actionName = "$SelectAll";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
    }
}