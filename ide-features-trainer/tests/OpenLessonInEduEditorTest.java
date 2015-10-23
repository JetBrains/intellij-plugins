import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightPlatformTestCase;
import training.editor.EduEditor;
import training.lesson.CourseManager;

/**
 * Created by karashevich on 22/10/15.
 */
public class OpenLessonInEduEditorTest extends LightPlatformTestCase {


//    public void testNoSdkException() throws Exception{
//        final Project project = LightPlatformTestCase.getProject();
//        final PsiFile file = LightPlatformTestCase.createFile("Test.java", "");
//        final VirtualFile vf = file.getVirtualFile();
//
//        boolean exceptionFactor = false;
//        try {
//                CourseManager.getInstance().checkEnvironment(project, CourseManager.getInstance().getCourses()[0]);
//        } catch (NoSdkException nse) {
//            exceptionFactor = true;
//        } finally {
//            assertTrue(exceptionFactor);
//        }
//    }

    public void testOpenLesson() throws Exception {

        final Project eduProject = CourseManager.getInstance().getEduProject();

        final PsiFile file = LightPlatformTestCase.createFile("Test.java", "");
        final VirtualFile vf = file.getVirtualFile();

        EduEditor eduEditor = null;
        try {
            eduEditor = CourseManager.getInstance().getEduEditor(eduProject, vf);
            assertNotNull(eduEditor);
        } finally {
            Disposer.dispose(eduEditor);
            eduProject.getProjectFile();

            com.intellij.ide.impl.ProjectUtil.closeAndDispose(eduProject);
        }
    }

}
