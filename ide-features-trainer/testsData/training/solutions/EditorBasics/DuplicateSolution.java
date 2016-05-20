package training.solutions.EditorBasics;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import training.commands.BadCommandException;
import training.commands.ExecutionList;
import training.learn.LessonProcessor;
import training.solutions.BaseSolutionClass;
import training.testFramework.LessonSolution;
import training.util.PerformActionUtil;

import java.util.concurrent.ExecutionException;

public class DuplicateSolution implements LessonSolution{

    @Override
    public void solveStep() throws InterruptedException, ExecutionException, BadCommandException {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        Editor editor = currentExecutionList.getEditor();
        if (currentExecutionList == null) return;

        int stepNumber = currentExecutionList.getElements().size() - 1;
        if (stepNumber == 1) {
            final String actionName = "EditorDuplicate";
            final AnAction action = com.intellij.openapi.actionSystem.ActionManager.getInstance().getAction(actionName);
            PerformActionUtil.performEditorAction(actionName, editor);
        }
        if (stepNumber == 0){
            BaseSolutionClass.gotoOffset(editor, 0);
            final String actionName = "EditorDownWithSelection";
            PerformActionUtil.performActionDisabledPresentation(actionName, editor);
            PerformActionUtil.performActionDisabledPresentation(actionName, editor);
            PerformActionUtil.performActionDisabledPresentation(actionName, editor);
            final String actionName2 = "EditorDuplicate";
            PerformActionUtil.performActionDisabledPresentation(actionName2, editor);
        }
    }
}