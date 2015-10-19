import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightPlatformTestCase;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import training.editor.EduEditor;
import training.lesson.CourseManager;

/**
 * Created by karashevich on 16/10/15.
 */



public class OpenEduEditorTest extends LightPlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }


    public void testSomething(){
        assertEquals(true, true);
    }

    public void testOpenEduEditor(){
        final Project project = LightPlatformTestCase.getProject();
        final PsiFile file = LightPlatformTestCase.createFile("Test.java", "");
        final VirtualFile vf = file.getVirtualFile();

        final EduEditor eduEditor = CourseManager.getInstance().getEduEditor(project, vf);
        assertEquals(eduEditor != null, true);
    }

}
