import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.impl.VirtualFilePointerManagerImpl;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.util.SmartList;
import com.intellij.util.ui.EdtInvocationManager;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import training.commands.*;
import training.editor.EduEditor;
import training.editor.EduEditorManager;
import training.lesson.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 28/10/15.
 */
@RunWith(Parameterized.class)
public class ExtendedSoftProjectBasedTest extends UsefulTestCase{

    protected Project myProject;
    protected VirtualFile myProjectRoot;
    protected String myProjectPath;

    protected Lesson myLesson;

    @Parameterized.Parameter(0) public String lessonId;

    private IdeaProjectTestFixture myProjectFixture;

    protected Sdk getProjectJDK() {
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

    protected void setUpLesson(){
        myLesson = CourseManagerOffline.getInstance().findLesson(lessonId);
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object> data(){
        List<Object> lessonsIds = new ArrayList<Object>();
        final Course[] courses = CourseManagerOffline.getInstance().getCourses();
        for (Course course : courses) {
            final ArrayList<Lesson> lessons = course.getLessons();
            for (Lesson lesson : lessons) {
                if (lesson.getCourse().courseType == Course.CourseType.PROJECT) {
                    lessonsIds.add(lesson.getName());
                }
            }
        }
        return lessonsIds;
    }

    @Before
    public void before() throws Exception {
        final Ref<Exception> ex = new Ref<Exception>();
        final List<Throwable> exceptions = new SmartList<Throwable>();

        Runnable runnable = new Runnable() {
            public void run() {

                try {
                    ExtendedSoftProjectBasedTest.super.setUp();
//                    myProjectFixture = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getTestName(true)).getFixture();

                    EduIdeaTestFixtureFactoryImpl.getFixtureFactory();
                    myProjectFixture = EduIdeaTestFixtureFactoryImpl.getFixtureFactory().createFixtureBuilder("ProjectLessonName").getFixture();
                    myProjectFixture.setUp();
                    myProject = myProjectFixture.getProject();
                    System.out.println(myProject);
                } catch (Exception e) {
                    ex.set(e);
                }
            }
        };
        invokeTestRunnable(runnable);
        final Exception exception = ex.get();
        if (exception != null) {
            throw exception;
        }
        if(exceptions.size() > 0){
            for (Throwable throwable : exceptions) {
                try {
                    throw throwable;
                } catch (Throwable throwable1) {
                    throwable1.printStackTrace();
                }
            }
        }

    }

    @After
    public void after ()throws Exception {
        final Ref<Exception> ex = new Ref<Exception>();
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    disposeAllEduEditors();
                    if(myProjectFixture != null) {
                        myProjectFixture.tearDown();
                    }
                } catch (Exception e) {
                    ex.set(e);
                } finally {
                    try {
                        myProjectFixture = null;
                        ExtendedSoftProjectBasedTest.super.tearDown();
                    } catch (Exception e) {
                        ex.set(e);
                    }
                }
            }
        };
        invokeTestRunnable(runnable);
        final Exception exception = ex.get();
        if (exception != null) {
            throw exception;
        }
    }

    @Test
    public void runSingle() throws Throwable {
        setUpLesson();
        prepareLesson();
        Runnable runnable = new Runnable() {
            public void run() {
                ((VirtualFilePointerManagerImpl) VirtualFilePointerManager.getInstance()).storePointers();

                try {
                    CourseManager.getInstance().checkEnvironment(myProject, myLesson.getCourse());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                assertNotNull(ProjectJdkTable.getInstance().findJdk(getProjectJDK().getName(), getProjectJDK().getSdkType().getName()));
                try {
                    CourseManager.getInstance().openLesson(myProject, myLesson);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        invokeTestRunnable(runnable);
        while(!myLesson.getPassed()){
            //pumpEvents
            EdtInvocationManager.getInstance().invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    com.intellij.util.ui.UIUtil.dispatchAllInvocationEvents();

                }
            });
            try {
                solveStep();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void solveStep() throws BadCommandException, ExecutionException, InterruptedException {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;
        final Element peekedCommandElement = currentExecutionList.getElements().peek();
        final Command.CommandType peekedCommandType = CommandFactory.buildCommand(peekedCommandElement).getCommandType();
        Command testCommand = new TestCommand();
        testCommand.execute(currentExecutionList);
    }



    private void disposeAllEduEditors() {
        final EduEditor[] allNotDisposedEduEditors = EduEditorManager.getInstance().getAllNotDisposedEduEditors();
        for (EduEditor eduEditor : allNotDisposedEduEditors) {
            Disposer.dispose(eduEditor);
        }
    }


    protected void prepareLesson(){
        assertNotNull(myLesson);
        myLesson.setPassed(false);
        assertTrue(!myLesson.getPassed());
    }


}
