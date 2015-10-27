import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.LightIdeaTestFixture;
import training.editor.EduEditor;
import training.lesson.CourseManager;

/**
 * Created by karashevich on 22/10/15.
 */
public class OpenLessonInEduEditorTest implements IdeaProjectTestFixture {


    @Override
    public Project getProject() {
        return null;
    }

    @Override
    public Module getModule() {
        return null;
    }

    @Override
    public void setUp() throws Exception {

    }

    @Override
    public void tearDown() throws Exception {

    }
}
