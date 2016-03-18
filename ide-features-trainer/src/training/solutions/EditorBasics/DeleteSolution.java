package training.solutions.EditorBasics;

import org.jdom.Element;
import training.commands.*;
import training.learn.LessonProcessor;
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
//            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor(), currentExecutionList.getEduEditor());
//            LightPlatformCodeInsightTestCase.executeAction(actionName, currentExecutionList.getEditor(), currentExecutionList.getProject());
            final Element peekedCommandElement = currentExecutionList.getElements().peek();
            final Command.CommandType peekedCommandType = CommandFactory.buildCommand(peekedCommandElement).getCommandType();
            Command testCommand = new TestCommand();
            try {
                testCommand.execute(currentExecutionList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}