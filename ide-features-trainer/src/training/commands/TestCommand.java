package training.commands;

import org.jetbrains.annotations.TestOnly;

import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 29/10/15.
 */
public class TestCommand extends Command {

    public TestCommand() {
        super(CommandType.TEST);
    }

    @TestOnly
    @Override
    public void execute(ExecutionList executionList) throws InterruptedException, ExecutionException, BadCommandException {
        startNextCommand(executionList);
    }
}
