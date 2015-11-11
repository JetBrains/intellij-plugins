import training.commands.BadCommandException;
import training.commands.Command;
import training.commands.ExecutionList;
import training.commands.TestCommand;
import training.lesson.LessonProcessor;
import training.testFramework.LessonSolution;

import java.util.concurrent.ExecutionException;

public class BasicCompletionSolution implements LessonSolution{

    @Override
    public void solveStep() throws InterruptedException, ExecutionException, BadCommandException {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;
        if(currentExecutionList)

        int stepNumber = currentExecutionList.getElements().size() - 1;
        if (stepNumber == 5) {
            currentExecutionList.getElements().poll();
            Command testCommand = new TestCommand();
            testCommand.execute(currentExecutionList);
        }
        if (stepNumber == 4){
            currentExecutionList.getElements().poll();
            Command testCommand = new TestCommand();
            testCommand.execute(currentExecutionList);
        }
        if (stepNumber == 2){
            currentExecutionList.getElements().poll();
            Command testCommand = new TestCommand();
            testCommand.execute(currentExecutionList);
        }
        if (stepNumber == 1){
            currentExecutionList.getElements().poll();
            Command testCommand = new TestCommand();
            testCommand.execute(currentExecutionList);
        }
    }
}