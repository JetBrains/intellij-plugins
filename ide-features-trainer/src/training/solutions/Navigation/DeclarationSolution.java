package training.solutions.Navigation;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import org.jdom.Element;
import training.commands.Command;
import training.commands.CommandFactory;
import training.commands.ExecutionList;
import training.commands.TestCommand;
import training.lesson.LessonProcessor;
import training.solutions.BaseSolutionClass;
import training.testFramework.LessonSolution;
import training.util.PerformActionUtil;

/**
 * Created by karashevich on 28/12/15.
 */
public class DeclarationSolution implements LessonSolution {
    @Override
    public void solveStep() throws Exception {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;

        int stepNumber = currentExecutionList.getElements().size() - 1;


        if (stepNumber == 2){
            PerformActionUtil.performActionDisabledPresentation(currentExecutionList.getEditor(), "com.intellij.codeInsight.navigation.actions.GotoDeclarationAction");
        }
        if (stepNumber == 0){
//            final String actionName = "GotoImplementation";
//            PerformActionUtil.performActionDisabledPresentation(actionName, currentExecutionList.getEditor());
            if (currentExecutionList == null) return;
            final Element peekedCommandElement = currentExecutionList.getElements().peek();
            final Command.CommandType peekedCommandType = CommandFactory.buildCommand(peekedCommandElement).getCommandType();
            Command testCommand = new TestCommand();
            testCommand.execute(currentExecutionList);
        }
    }
}