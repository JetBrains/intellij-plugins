import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.CollectingAsyncResult;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.impl.VirtualFilePointerManagerImpl;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import org.apache.velocity.anakia.AnakiaElement;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import training.commands.*;
import training.editor.EduEditor;
import training.lesson.*;
import training.lesson.exceptons.NoSdkException;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 29/10/15.
 */
@RunWith(com.intellij.testFramework.Parameterized.class)
public class AllLessonsTest extends EduLessonTest {

    @Parameterized.Parameter(0)
    private String lessonId;

    @Parameterized.Parameters(name = "{0}")
    public static List<Object> params() throws Throwable {
        return Collections.emptyList();
    }

    @com.intellij.testFramework.Parameterized.Parameters(name = "{0}")
    public static List<Object> params(@NotNull Class<?> klass) throws Throwable{
        List<Object> lessonsIds = new ArrayList<Object>();
        final Course[] courses = CourseManagerOffline.getInstance().getCourses();
        for (Course course : courses) {
            final ArrayList<Lesson> lessons = course.getLessons();
            for (Lesson lesson : lessons) {
                lessonsIds.add(lesson.getName());
            }
        }
        return lessonsIds;
    }

    public AllLessonsTest() {
        this.lessonId = lessonId;
    }

//    public AllLessonsTest(String lessonId) {
//        this.lessonId = lessonId;
//    }

    @Override
    protected void setUpLesson() {
        //myLesson is already set
    }

    //solved step softly, all user actions are mocked up by invocation next command
    @Override
    public void solveStep() throws InterruptedException, ExecutionException, BadCommandException {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;
        int stepNumber = currentExecutionList.getElements().size() - 1;
        final Element peekedCommandElement = currentExecutionList.getElements().peek();
        final Command.CommandType peekedCommandType = CommandFactory.buildCommand(peekedCommandElement).getCommandType();
        if (peekedCommandType == Command.CommandType.TRY || peekedCommandType == Command.CommandType.WIN)
        currentExecutionList.getElements().poll();
        Command testCommand = new TestCommand();
        testCommand.execute(currentExecutionList);
    }

    @Override
    protected String getTestName(boolean lowercaseFirstLetter) {
        final Lesson lesson = CourseManagerOffline.getInstance().findLesson(lessonId);
        String courseName = lesson.getCourse().getName();
        if (lowercaseFirstLetter) {
            StringBuilder sb = new StringBuilder();
            String firstLetter = new String(String.valueOf(courseName.charAt(0)));
            sb.append(firstLetter.toLowerCase()).append(courseName.subSequence(1, courseName.length() - 1));
            courseName = sb.toString();
        }
        return courseName + "_" + lessonId + "_Solution_Test";
    }

    @Test
    public void testLesson() throws Exception{
        ((VirtualFilePointerManagerImpl) VirtualFilePointerManager.getInstance()).storePointers();

        EduEditor eduEditor = null;
        boolean noSdkDetected = false;
        try {
            CourseManager.getInstance().checkEnvironment(myProject, myLesson.getCourse());
        } catch (NoSdkException e) {
            noSdkDetected = true;
        }
        assertTrue(noSdkDetected);
        noSdkDetected = false;
        final Boolean result = ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
            @Override
            public Boolean compute() {
                ProjectRootManager.getInstance(myProject).setProjectSdk(getProjectJDK());
                return true;
            }
        });
        if (result) {
            assertNotNull(ProjectJdkTable.getInstance().findJdk(getProjectJDK().getName(), getProjectJDK().getSdkType().getName()));
            CourseManager.getInstance().openLesson(myProject, myLesson);
        } else {
            assertTrue(result); //Unable to add new
        }
    }
}
