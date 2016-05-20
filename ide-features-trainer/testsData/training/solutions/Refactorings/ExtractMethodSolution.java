package training.solutions.Refactorings;

import org.jdom.Element;
import training.commands.Command;
import training.commands.CommandFactory;
import training.commands.ExecutionList;
import training.commands.TestCommand;
import training.learn.LessonProcessor;
import training.testFramework.LessonSolution;

/**
 * Created by karashevich on 23/12/15.
 */
public class ExtractMethodSolution implements LessonSolution {
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
