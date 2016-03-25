package training.solutions.Navigation;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import org.jdom.Element;
import training.commands.Command;
import training.commands.CommandFactory;
import training.commands.ExecutionList;
import training.commands.TestCommand;
import training.learn.LessonProcessor;
import training.testFramework.LessonSolution;
import training.util.PerformActionUtil;

/**
 * Created by karashevich on 28/12/15.
 */
public class OccurrencesSolution implements LessonSolution {
    @Override
    public void solveStep() throws Exception {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;

        int stepNumber = currentExecutionList.getElements().size() - 1;


        if (stepNumber == 4){
            //preselect cellphone
            final Editor editor = currentExecutionList.getEditor();
            int start_line = 8;
            int start_column = 42;
            int end_line = 8;
            int end_column = 51;
            start_line--;
            start_column--;
            end_column--;
            end_line--;
            final LogicalPosition blockStart = new LogicalPosition(start_line, start_column);
            final LogicalPosition blockEnd = new LogicalPosition(end_line, end_column);

            editor.getSelectionModel().setSelection(editor.logicalPositionToOffset(blockStart), editor.logicalPositionToOffset(blockEnd));

            final String actionName = "Find";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
        if (stepNumber == 3){
            final Element peekedCommandElement = currentExecutionList.getElements().poll();
            final Command.CommandType peekedCommandType = CommandFactory.buildCommand(peekedCommandElement).getCommandType();
            Command testCommand = new TestCommand();
            testCommand.execute(currentExecutionList);
        }
        if (stepNumber == 2){
            final Element peekedCommandElement = currentExecutionList.getElements().poll();
            final Command.CommandType peekedCommandType = CommandFactory.buildCommand(peekedCommandElement).getCommandType();
            Command testCommand = new TestCommand();
            testCommand.execute(currentExecutionList);
        }
        if (stepNumber == 1){
            final Element peekedCommandElement = currentExecutionList.getElements().poll();
            final Command.CommandType peekedCommandType = CommandFactory.buildCommand(peekedCommandElement).getCommandType();
            Command testCommand = new TestCommand();
            testCommand.execute(currentExecutionList);
        }
        if (stepNumber == 0){
            final String actionName = "FindPrevious";
            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
        }
    }
}
