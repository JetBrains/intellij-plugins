package training.testFramework;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
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

/**
 * Created by karashevich on 28/10/15.
 */
public abstract class EduLessonTest extends UsefulTestCase implements LessonSolution{

    protected Project myProject;
    protected VirtualFile myProjectRoot;
    protected String myProjectPath;

    protected Lesson myLesson;

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

    protected abstract void setUpLesson();

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
            setUpLesson();
            prepareLesson();
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
        Throwable exception = null;
        setUp();

        try {
            runTest();
            while(!myLesson.getPassed()){
                //pumpEvents
                com.intellij.util.ui.UIUtil.dispatchAllInvocationEvents();
                solveStep();
            }

        } catch (Throwable running) {
            exception = running;
        } finally {
            try {
                tearDown();
            } catch (Throwable tearingDown) {
                if (exception == null) exception = tearingDown;
            }
        }
        if (exception != null) throw exception;
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

    protected void prepareLesson(){
        assertNotNull(myLesson);
        myLesson.setPassed(false);
        assertTrue(!myLesson.getPassed());
    }


}
