package training.solutions.Completions;

import com.intellij.openapi.command.WriteCommandAction;
import training.commands.ExecutionList;
import training.lesson.LessonProcessor;
import training.testFramework.LessonSolution;
import training.util.PerformActionUtil;

/**
 * Created by karashevich on 17/12/15.
 */
public class SmartTypeCompletionSolution implements LessonSolution{

    @Override
    public void solveStep() throws Exception {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;

        int stepNumber = currentExecutionList.getElements().size() - 1;
        if (stepNumber == 6){
            final String actionName = "SmartTypeCompletion";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
            final String action2Name = "EditorChooseLookupItem";
            PerformActionUtil.performActionDisabledPresentation(action2Name, currentExecutionList.getEditor());
//            INVOKE: Basic Completion
        }
        if (stepNumber == 2){
            final String actionName = "SmartTypeCompletion";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
            final String action2Name = "SmartTypeCompletion";
            PerformActionUtil.performActionDisabledPresentation(action2Name, currentExecutionList.getEditor());
            final String action3Name = "EditorChooseLookupItem";
            PerformActionUtil.performActionDisabledPresentation(action3Name, currentExecutionList.getEditor());
//            PRESS: Editor Enter
        }
    }
}
