package training.commands;

import com.intellij.openapi.editor.Editor;
import training.learn.Lesson;
import training.learn.LessonManager;

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
        Lesson lesson = executionList.getLesson();
        Editor editor = executionList.getEditor();
        LessonManager.getInstance(lesson).grabMouseActions(editor);

        executionList.getElements().poll();
        startNextCommand(executionList);

    }
}
