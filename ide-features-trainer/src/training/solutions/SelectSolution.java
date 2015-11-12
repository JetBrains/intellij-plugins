package training.solutions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.wm.IdeFocusManager;
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
//            final ActionManagerEx amex = ActionManagerEx.getInstanceEx();
//            EditorActionManager.getInstance().getActionHandler(actionName).execute(currentExecutionList.getEditor(), null, DataContext.EMPTY_CONTEXT);
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