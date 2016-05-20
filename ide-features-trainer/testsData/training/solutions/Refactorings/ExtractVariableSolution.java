package training.solutions.Refactorings;

import org.jdom.Element;
import training.commands.Command;
import training.commands.CommandFactory;
import training.commands.ExecutionList;
import training.commands.TestCommand;
import training.learn.LessonProcessor;
import training.testFramework.LessonSolution;
import training.util.PerformActionUtil;

/**
 * Created by karashevich on 23/12/15.
 */
public class ExtractVariableSolution implements LessonSolution {

    public void solveStepByStep() throws Exception {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;

        int stepNumber = currentExecutionList.getElements().size() - 1;
        if (stepNumber == 2) {
            final String actionName = "IntroduceVariable";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
            final String actionName2 = "NextTemplateVariable";
            PerformActionUtil.performActionDisabledPresentation(actionName2, currentExecutionList.getEditor());
        }
    }

    @Override
    public void solveStep() throws Exception {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;
        final Element peekedCommandElement = currentExecutionList.getElements().peek();
        final Command.CommandType peekedCommandType = CommandFactory.buildCommand(peekedCommandElement).getCommandType();
        Command testCommand = new TestCommand();
        testCommand.execute(currentExecutionList);
    }
}
