package training.commands;


/**
 * Created by karashevich on 30/01/15.
 */
public class NoCommand extends Command {

    public NoCommand(){
        super(CommandType.NOCOMMAND);
    }

    @Override
    public void execute(ExecutionList executionList) {
        //do nothing
    }
}
