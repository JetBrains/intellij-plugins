package training.commands;

/**
 * Created by karashevich on 30/01/15.
 */
public class CaretUnblockCommand extends Command {

    public CaretUnblockCommand(){
        super(CommandType.CARETUNBLOCK);
    }

    @Override
    public void execute(ExecutionList executionList) {
        //Unblock caret and perform next command
        executionList.getEduEditor().unblockCaret();
        executionList.getElements().poll();
        startNextCommand(executionList);

    }
}
