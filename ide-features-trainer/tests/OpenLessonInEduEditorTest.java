import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightPlatformTestCase;
import training.editor.EduEditor;
import training.lesson.CourseManager;
import training.lesson.Lesson;
import training.lesson.exceptons.NoSdkException;

/**
 * Created by karashevich on 22/10/15.
 */
public class OpenLessonInEduEditorTest extends LightPlatformTestCase {

//    public void testOpenLessonInEduEditor() throws Exception{
//        final Project eduProject = CourseManager.getInstance().getEduProject();
//
//        try {
//
//
//
//            final Lesson lesson = CourseManager.getInstance().getCourses()[0].getLessons().get(0);
//            CourseManager.getInstance().openLesson(project, lesson);
//
//            assertNotNull(eduEditor);
//        } finally {
//            Disposer.dispose(eduEditor);
//        }
//    }

    public void testNoSdkException() throws Exception{
        final Project project = LightPlatformTestCase.getProject();
        final PsiFile file = LightPlatformTestCase.createFile("Test.java", "");
        final VirtualFile vf = file.getVirtualFile();

        boolean exceptionFactor = false;
        try {
                CourseManager.getInstance().checkEnvironment(project, CourseManager.getInstance().getCourses()[0]);

        } catch (NoSdkException nse) {
            exceptionFactor = true;
        } finally {
            assertTrue(exceptionFactor);
        }
    }


}
