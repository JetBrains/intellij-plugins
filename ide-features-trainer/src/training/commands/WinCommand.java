package training.commands;

import org.jdom.Element;

/**
 * Created by karashevich on 30/01/15.
 */
public class WinCommand extends Command {

    public WinCommand(){
        super(CommandType.WIN);
    }

    @Override
    public void execute(ExecutionList executionList) throws InterruptedException {

        Element element = executionList.getElements().poll();
        executionList.getLesson().pass();
        executionList.getEduEditor().passLesson(executionList.getLesson());

    }
}
