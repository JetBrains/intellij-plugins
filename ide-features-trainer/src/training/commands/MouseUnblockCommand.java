package training.commands;


import com.intellij.openapi.editor.Editor;
import training.learn.Lesson;
import training.learn.LessonManager;

/**
 * Created by karashevich on 30/01/15.
 */
public class MouseUnblockCommand extends Command {

    public MouseUnblockCommand(){
        super(CommandType.MOUSEUNBLOCK);
    }

    @Override
    public void execute(ExecutionList executionList) {
        executionList.getElements().poll();

        //Unblock mouse and perform next
        Lesson lesson = executionList.getLesson();
        Editor editor = executionList.getEditor();

        LessonManager.getInstance(lesson).unblockMouse(editor);
        startNextCommand(executionList);
    }
}
