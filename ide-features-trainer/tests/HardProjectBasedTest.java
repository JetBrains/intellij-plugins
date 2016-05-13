import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.impl.VirtualFilePointerManagerImpl;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.ui.EdtInvocationManager;
import com.intellij.util.ui.UIUtil;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import training.commands.Command;
import training.commands.CommandFactory;
import training.commands.ExecutionList;
import training.commands.TestCommand;
import training.learn.*;
import training.testFramework.LearnIdeaTestFixtureFactoryImpl;
import training.testFramework.LessonSolution;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karashevich on 28/12/15.
 */
@RunWith(Parameterized.class)
public class HardProjectBasedTest extends UsefulTestCase {

    private Project myProject;
    private HardProjectBasedTest myCase;
    private IdeaProjectTestFixture myProjectFixture;
    private Lesson myLesson;
    private LessonSolution myLessonSolution;

    @Parameterized.Parameter(0)
    private String lessonId;

    @Parameterized.Parameters(name = "{0}")
    public static List<Object> data() {
        List<Object> lessonsIds = new ArrayList<Object>();
        final Module[] modules = CourseManagerTest.getInstance().getModules();
        assert modules != null;
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

    @Before
    public void setUp() throws Exception {
        final Ref<Exception> ex = new Ref<Exception>();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    HardProjectBasedTest.super.setUp();
                    LearnIdeaTestFixtureFactoryImpl.getFixtureFactory();

                    myProjectFixture = LearnIdeaTestFixtureFactoryImpl.getFixtureFactory().createFixtureBuilder(getTestName(true)).getFixture();
                    myProjectFixture.setUp();
                    myProject = myProjectFixture.getProject();

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

    }

    @After
    public void tearDown() throws Exception {
        final Ref<Exception> ex = new Ref<Exception>();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        if(myProjectFixture != null) {
                            myProjectFixture.tearDown();
                            myProjectFixture = null;
                        }

                        UIUtil.dispatchAllInvocationEvents();
                    } catch (Exception e) {
                        ex.set(e);
                    }
                } catch (Exception e) {
                    ex.set(e);
                } finally {
                    try {
                        HardProjectBasedTest.super.tearDown();
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


    private Sdk getProjectJDK() {
        JavaSdk javaSdk = JavaSdk.getInstance();
        final String suggestedHomePath = javaSdk.suggestHomePath();
        final String versionString = javaSdk.getVersionString(suggestedHomePath);
        assert versionString != null;
        assert suggestedHomePath != null;
        final Sdk newJdk = javaSdk.createJdk(javaSdk.getVersion(versionString).name(), suggestedHomePath);

        final Sdk foundJdk = ProjectJdkTable.getInstance().findJdk(newJdk.getName(), newJdk.getSdkType().getName());
        if (foundJdk == null) {
            ProjectJdkTable.getInstance().addJdk(newJdk);
        }
        return newJdk;
    }

    private void setUpLesson() {
        myLesson = CourseManagerTest.getInstance().findLesson(lessonId);
    }

    private void setUpSolution() throws Exception {
        myLessonSolution = CourseManagerTest.getInstance().findSolution(lessonId);
        assertNotNull(myLessonSolution);
    }

    @Test
    public void runSingle() throws Throwable {
        setUpLesson();
        setUpSolution();
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
        while(!myLesson.getPassed()){
            //pumpEvents
            EdtInvocationManager.getInstance().invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    com.intellij.util.ui.UIUtil.dispatchAllInvocationEvents();

                }
            });
            if (myLesson.getPassed()) break;
            EdtTestUtil.runInEdtAndWait(new ThrowableRunnable<Throwable>() {
                @Override
                public void run() {
                    try {
                        myLessonSolution.solveStep();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
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

    private void prepareLesson() {
        assertNotNull(myLesson);
        myLesson.setPassed(false);
        assertTrue(!myLesson.getPassed());
    }


}

