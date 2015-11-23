package training.solutions.EditorBasics;

import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import training.commands.BadCommandException;
import training.commands.ExecutionList;
import training.lesson.LessonProcessor;
import training.testFramework.LessonSolution;
import training.util.PerformActionUtil;

import java.util.concurrent.ExecutionException;

public class DeleteSolution implements LessonSolution{

    @Override
    public void solveStep() throws InterruptedException, ExecutionException, BadCommandException {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;

        int stepNumber = currentExecutionList.getElements().size() - 1;
        if (stepNumber == 1){
            final String actionName = "EditorDeleteLine";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 0){
            final String actionName = "$Undo";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor(), currentExecutionList.getEduEditor());
//            LightPlatformCodeInsightTestCase.executeAction(actionName, currentExecutionList.getEditor(), currentExecutionList.getProject());
        }
    }
}