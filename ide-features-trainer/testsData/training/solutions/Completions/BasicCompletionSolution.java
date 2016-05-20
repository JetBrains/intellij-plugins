package training.solutions.Completions;

import com.intellij.openapi.command.WriteCommandAction;
import training.commands.ExecutionList;
import training.learn.LessonProcessor;
import training.testFramework.LessonSolution;
import training.util.PerformActionUtil;

/**
 * Created by karashevich on 17/12/15.
 */
public class BasicCompletionSolution implements LessonSolution{

    @Override
    public void solveStep() throws Exception {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;

        int stepNumber = currentExecutionList.getElements().size() - 1;
        if (stepNumber == 7) {
//            TYPE: Ran
            final String finalText = "Ran";
            boolean isTyping = true;
            final int[] i = {0};
            final int initialOffset = currentExecutionList.getEditor().getCaretModel().getOffset();

            while (isTyping) {
                final int finalI = i[0];
                WriteCommandAction.runWriteCommandAction(currentExecutionList.getProject(), new Runnable() {
                    @Override
                    public void run() {
                        currentExecutionList.getEditor().getDocument().insertString(finalI + initialOffset, finalText.subSequence(i[0], i[0] + 1));
                        currentExecutionList.getEditor().getCaretModel().moveToOffset(finalI + 1 + initialOffset);
                    }
                });
                isTyping = (++i[0] < finalText.length());
            }
            final String actionName = "EditorChooseLookupItem";
            PerformActionUtil.performEditorAction(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 6){
            final String actionName = "CodeCompletion";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
//            INVOKE: Basic Completion
        }
        if (stepNumber == 5){
            final String actionName = "EditorChooseLookupItem";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
//            PRESS: Editor Enter
        }
        if (stepNumber == 2){
            final String actionName = "CodeCompletion";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
//            Basic Completion Twice
        }
    }
}
