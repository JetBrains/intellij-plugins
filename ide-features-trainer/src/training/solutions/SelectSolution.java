package training.solutions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jdesktop.swingx.action.ActionManager;
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
            final AnAction action = com.intellij.openapi.actionSystem.ActionManager.getInstance().getAction(actionName);
//            action.actionPerformed();
//            PerformActionUtil.performAction(actionName, currentExecutionList.getEditor(), currentExecutionList.getProject());
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