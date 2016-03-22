package training.solutions.EditorBasics;

import training.commands.BadCommandException;
import training.commands.ExecutionList;
import training.learn.LessonProcessor;
import training.solutions.BaseSolutionClass;
import training.testFramework.LessonSolution;
import training.util.PerformActionUtil;

import java.util.concurrent.ExecutionException;

public class CommentSolution implements LessonSolution{

    @Override
    public void solveStep() throws InterruptedException, ExecutionException, BadCommandException {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;

        int stepNumber = currentExecutionList.getElements().size() - 1;
        if (stepNumber == 3){
            final String actionName = "CommentByLineComment";
            BaseSolutionClass.gotoOffset(currentExecutionList.getEditor(), 0);
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 1){
            final String actionName = "CommentByLineComment";
            BaseSolutionClass.gotoOffset(currentExecutionList.getEditor(), 0);
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());

        }
        if (stepNumber == 0){
            BaseSolutionClass.gotoOffset(currentExecutionList.getEditor(), 0);
            final String actionName = "EditorDownWithSelection";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
            final String action2Name = "CommentByLineComment";
            PerformActionUtil.performActionDisabledPresentation(action2Name, currentExecutionList.getEditor());
        }
    }
}