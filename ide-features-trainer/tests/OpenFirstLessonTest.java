import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.impl.VirtualFilePointerManagerImpl;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import training.commands.BadCommandException;
import training.editor.EduEditor;
import training.lesson.Course;
import training.lesson.CourseManager;
import training.lesson.Lesson;
import training.lesson.exceptons.NoSdkException;

import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 29/10/15.
 */
public class OpenFirstLessonTest extends EduLessonTest {

    public void testOpenLessonInEduEditor() throws Exception{
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

    @Override
    protected void setUpLesson() {
        final Course[] courses = CourseManager.getInstance().getCourses();
        assertTrue(courses != null && courses.length > 0);
        Lesson lesson = null;
        for (Course course : courses) {
            if (!course.getLessons().isEmpty()) {
                lesson = course.getLessons().get(0);
                break;
            }
        }
        myLesson = lesson;
    }


    @Override
    public void solveStep() throws InterruptedException, ExecutionException, BadCommandException {
        //do Nothing
    }
}
