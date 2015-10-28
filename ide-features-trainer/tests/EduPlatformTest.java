import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.impl.VirtualFilePointerManagerImpl;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.editor.EduEditor;
import training.editor.EduEditorManager;
import training.lesson.Course;
import training.lesson.CourseManager;
import training.lesson.Lesson;
import training.lesson.exceptons.NoSdkException;

/**
 * Created by karashevich on 28/10/15.
 */
public class EduPlatformTest extends UsefulTestCase {

    protected Project myProject;
    protected VirtualFile myProjectRoot;
    protected String myProjectPath;

    private IdeaProjectTestFixture myProjectFixture;

    protected Sdk getProjectJDK() {
//        return super.getProjectJDK();
        JavaSdk javaSdk = JavaSdk.getInstance();
        final String suggestedHomePath = javaSdk.suggestHomePath();
        final String versionString = javaSdk.getVersionString(suggestedHomePath);
        final Sdk newJdk = javaSdk.createJdk(javaSdk.getVersion(versionString).name(), suggestedHomePath);

        final Sdk foundJdk = ProjectJdkTable.getInstance().findJdk(newJdk.getName(), newJdk.getSdkType().getName());
        if (foundJdk == null) {
            ProjectJdkTable.getInstance().addJdk(newJdk);
        }
        return newJdk;
    }

    @NotNull
    protected ModuleType getModuleType() {
        return StdModuleTypes.JAVA;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        try {
            myProjectFixture = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getTestName()).getFixture();
            myProjectFixture.setUp();

        } catch (Exception e) {
            super.tearDown();
            throw e;
        }

        try{
            myProject = myProjectFixture.getProject();
            myProjectRoot = myProject.getBaseDir();
            myProjectPath = myProjectRoot.getPath();
        } catch (Exception e) {
            tearDown();
            throw e;
        }
    }

    @Override
    public void tearDown ()throws Exception {
        try {
            disposeAllEduEditors();
            if(myProjectFixture != null) {
                myProjectFixture.tearDown();
            }
        }
        finally {
            super.tearDown();
        }
    }

    @Override
    protected void defaultRunBare() throws Throwable {
        super.defaultRunBare();
    }

    private void disposeAllEduEditors() {
        final EduEditor[] allNotDisposedEduEditors = EduEditorManager.getInstance().getAllNotDisposedEduEditors();
        for (EduEditor eduEditor : allNotDisposedEduEditors) {
            Disposer.dispose(eduEditor);
        }
    }

    private String getTestName() {
        return getTestName(true);
    }

    public void testFindAtLeastOneLesson() throws Exception {
        Lesson lesson = getAnyLesson();
        assertNotNull(lesson);
    }

    @Nullable
    private Lesson getAnyLesson() {
        final Course[] courses = CourseManager.getInstance().getCourses();
        assertTrue(courses != null && courses.length > 0);
        Lesson lesson = null;
        for (Course course : courses) {
            if (!course.getLessons().isEmpty()) {
                lesson = course.getLessons().get(0);
                break;
            }
        }
        return lesson;
    }


}
