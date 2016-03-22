package training.commands;

import com.intellij.openapi.editor.Editor;
import training.learn.Lesson;
import training.learn.LessonManager;

/**
 * Created by karashevich on 30/01/15.
 */
public class CaretBlockCommand extends Command {

    public CaretBlockCommand(){
        super(CommandType.CARETBLOCK);
    }

    @Override
    public void execute(ExecutionList executionList) {
        executionList.getElements().poll();

        Lesson lesson = executionList.getLesson();
        Editor editor = executionList.getEditor();

        //Block caret and perform next command
        LessonManager.getInstance(lesson).blockCaret(editor);
        startNextCommand(executionList);

    }
}
