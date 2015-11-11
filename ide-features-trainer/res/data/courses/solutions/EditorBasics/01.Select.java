import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import training.commands.*;
import training.lesson.LessonProcessor;
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
            PerformActionUtil.performAction(actionName, currentExecutionList.getEditor(), currentExecutionList.getProject());
        }
        if (stepNumber == 3){
            final String actionName = "EditorSelectWord";
            PerformActionUtil.performAction(actionName, currentExecutionList.getEditor(), currentExecutionList.getProject());
        }
        if (stepNumber == 2){
            final String actionName = "EditorUnSelectWord";
            PerformActionUtil.performAction(actionName, currentExecutionList.getEditor(), currentExecutionList.getProject());
        }
        if (stepNumber == 1){
            final String actionName = "$SelectAll";
            PerformActionUtil.performAction(actionName, currentExecutionList.getEditor(), currentExecutionList.getProject());
        }
    }
}