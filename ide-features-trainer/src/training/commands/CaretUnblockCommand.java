package training.commands;

import training.learn.Lesson;
import training.learn.LessonManager;

/**
 * Created by karashevich on 30/01/15.
 */
public class CaretUnblockCommand extends Command {

    public CaretUnblockCommand(){
        super(CommandType.CARETUNBLOCK);
    }

    @Override
    public void execute(ExecutionList executionList) {
        executionList.getElements().poll();

        Lesson lesson = executionList.getLesson();

        //Unblock caret and perform next command
        LessonManager.getInstance(lesson).unblockCaret();
        startNextCommand(executionList);

    }
}
