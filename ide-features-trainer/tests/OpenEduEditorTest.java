import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightPlatformTestCase;
import training.editor.EduEditor;
import training.lesson.CourseManager;

/**
 * Created by karashevich on 16/10/15.
 */

public class OpenEduEditorTest extends LightPlatformTestCase {



    public void testOpenEduEditor() throws Exception {
        final Project project = LightPlatformTestCase.getProject();
        final PsiFile file = LightPlatformTestCase.createFile("Test.java", "");
        final VirtualFile vf = file.getVirtualFile();

        EduEditor eduEditor = null;
        try {
            eduEditor = CourseManager.getInstance().getEduEditor(project, vf);
            assertNotNull(eduEditor);
        } finally {
            Disposer.dispose(eduEditor);
        }
    }
}
