package org.jetbrains.training.commands;


/**
 * Created by karashevich on 30/01/15.
 */
public class MouseUnblockCommand extends Command {

    public MouseUnblockCommand(){
        super(CommandType.MOUSEUNBLOCK);
    }

    @Override
    public void execute(ExecutionList executionList) {
        //Unblock mouse and perform next
        executionList.getEduEditor().restoreMouseActions();
        executionList.getElements().poll();
        startNextCommand(executionList);
    }
}
