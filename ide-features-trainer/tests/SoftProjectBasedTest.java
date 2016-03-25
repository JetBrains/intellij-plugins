import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.impl.VirtualFilePointerManagerImpl;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.util.ui.EdtInvocationManager;
import org.jdom.Element;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import training.commands.*;
import training.learn.*;
import training.testFramework.LearnIdeaTestFixtureFactoryImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karashevich on 28/10/15.
 */
@RunWith(Parameterized.class)
public class SoftProjectBasedTest extends UsefulTestCase {

    private static Project myProject;
    protected VirtualFile myProjectRoot;
    protected String myProjectPath;

    private static SoftProjectBasedTest myCase;

    private Lesson myLesson;

    @Parameterized.Parameter(0)
    private String lessonId;

    private static IdeaProjectTestFixture myProjectFixture;

    private Sdk getProjectJDK() {
        JavaSdk javaSdk = JavaSdk.getInstance();
        final String suggestedHomePath = javaSdk.suggestHomePath();
        final String versionString = javaSdk.getVersionString(suggestedHomePath);
        assert versionString != null;
        final Sdk newJdk = javaSdk.createJdk(javaSdk.getVersion(versionString).name(), suggestedHomePath);

        final Sdk foundJdk = ProjectJdkTable.getInstance().findJdk(newJdk.getName(), newJdk.getSdkType().getName());
        if (foundJdk == null) {
            ProjectJdkTable.getInstance().addJdk(newJdk);
        }
        return newJdk;
    }

    protected void setUpLesson() {
        myLesson = CourseManagerWithoutIDEA.getInstance().findLesson(lessonId);
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object> data() {
        return dataA();
    }

    public static List<Object> dataA() {
        List<Object> lessonsIds = new ArrayList<Object>();
        final Module[] modules = CourseManagerWithoutIDEA.getInstance().getModules();
        for (Module module : modules) {
            final ArrayList<Lesson> lessons = module.getLessons();
            for (Lesson lesson : lessons) {
                if (lesson.getModule().moduleType == Module.ModuleType.PROJECT) {
                    lessonsIds.add(lesson.getName());
                }
            }
        }
        return lessonsIds;
    }


    @BeforeClass
    public static void before() throws Exception {

        myCase = new SoftProjectBasedTest();

        myCase.setUp();

        LearnIdeaTestFixtureFactoryImpl.getFixtureFactory();
        myProjectFixture = LearnIdeaTestFixtureFactoryImpl.getFixtureFactory().createFixtureBuilder("ProjectLessonName").getFixture();
        myProjectFixture.setUp();
        myProject = myProjectFixture.getProject();
    }

    @AfterClass
    public static void last() throws Exception {
        final Ref<Exception> ex = new Ref<Exception>();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        myProjectFixture.tearDown();
                        myProjectFixture = null;
                    } catch (Exception e) {
                        ex.set(e);
                    }
                } catch (Exception e) {
                    ex.set(e);
                } finally {
                    try {
                        myCase.tearDown();
                    } catch (Exception e) {
                        ex.set(e);
                    }
                }
            }
        };
        myCase.invokeTestRunnable(runnable);
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
                    CourseManager.getInstance().checkEnvironment(myProject, myLesson.getModule());
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
        while (!myLesson.getPassed()) {
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

    private void solveStep() throws Exception {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;
        final Element peekedCommandElement = currentExecutionList.getElements().peek();
        final Command.CommandType peekedCommandType = CommandFactory.buildCommand(peekedCommandElement).getCommandType();
        Command testCommand = new TestCommand();
        testCommand.execute(currentExecutionList);
    }




    protected void prepareLesson() {
        assertNotNull(myLesson);
        myLesson.setPassed(false);
        assertTrue(!myLesson.getPassed());
    }


}
