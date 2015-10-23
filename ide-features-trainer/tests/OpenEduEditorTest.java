import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.project.ProjectUtilCore;
import com.intellij.openapi.projectRoots.impl.ProjectRootUtil;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.impl.VirtualFilePointerContainerImpl;
import com.intellij.openapi.vfs.impl.VirtualFilePointerManagerImpl;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerContainer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
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

    public void testOpenEduEditorInEduProject() throws Exception {

        CourseManager.getInstance().setTestModeFlag(true);
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
            final VirtualFilePointerManagerImpl instanceImpl = (VirtualFilePointerManagerImpl) VirtualFilePointerManagerImpl.getInstance();
            final VirtualFilePointer[] pointersUnder = instanceImpl.getPointersUnder(eduProject.getBaseDir(), "");
            for (VirtualFilePointer virtualFilePointer : pointersUnder) {

                virtualFilePointer = null;
            }
            com.intellij.ide.impl.ProjectUtil.closeAndDispose(eduProject);

//            instanceImpl.getPointersUnder()
            CourseManager.getInstance().setTestModeFlag(false);
        }
    }

}
