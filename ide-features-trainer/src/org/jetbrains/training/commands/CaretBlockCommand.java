package org.jetbrains.training.commands;

/**
 * Created by karashevich on 30/01/15.
 */
public class CaretBlockCommand extends Command {

    public CaretBlockCommand(){
        super(CommandType.CARETBLOCK);
    }

    @Override
    public void execute(ExecutionList executionList) {
        //Block caret and perform next command
        executionList.getEduEditor().blockCaret();

        executionList.getElements().poll();
        startNextCommand(executionList);

    }
}
