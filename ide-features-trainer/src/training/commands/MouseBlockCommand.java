package training.commands;

/**
 * Created by karashevich on 30/01/15.
 */
public class MouseBlockCommand extends Command {

    public MouseBlockCommand(){
        super(CommandType.MOUSEBLOCK);
    }

    @Override
    public void execute(ExecutionList executionList) {
        //Block mouse and perform next
        executionList.getEduEditor().grabMouseActions();

        executionList.getElements().poll();
        startNextCommand(executionList);

    }
}
