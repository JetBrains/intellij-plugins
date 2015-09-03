package org.jetbrains.training.commands;

/**
 * Created by karashevich on 30/01/15.
 */
public class ShowLineNumberCommand extends Command {

    public ShowLineNumberCommand(){
        super(CommandType.SHOWLINENUMBER);
    }

    @Override
    public void execute(ExecutionList executionList) {
        //Block caret and perform next command
//        ActionManager.getInstance().getAction()
        executionList.getEditor().getSettings().setLineNumbersShown(true);
        executionList.getElements().poll();
        startNextCommand(executionList);
    }
}
