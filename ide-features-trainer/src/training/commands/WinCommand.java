package training.commands;

import com.intellij.openapi.project.Project;
import org.jdom.Element;
import training.learn.Lesson;
import training.learn.LessonManager;

/**
 * Created by karashevich on 30/01/15.
 */
public class WinCommand extends Command {

    public WinCommand(){
        super(CommandType.WIN);
    }

    @Override
    public void execute(ExecutionList executionList) throws InterruptedException {

        executionList.getElements().poll();

        Project project = executionList.getProject();
        Lesson lesson = executionList.getLesson();
        lesson.pass();
        LessonManager.getInstance(lesson).passLesson(project);
    }
}
