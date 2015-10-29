import training.commands.BadCommandException;
import training.lesson.Lesson;

import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 29/10/15.
 */
public interface LessonSolution {

    void solveStep() throws InterruptedException, ExecutionException, BadCommandException;

}