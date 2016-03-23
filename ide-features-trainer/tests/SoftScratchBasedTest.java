import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.impl.VirtualFilePointerManagerImpl;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.util.ui.EdtInvocationManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import training.commands.*;
import training.learn.*;
import training.learn.exceptons.*;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 28/10/15.
 */
@RunWith(Parameterized.class)
public class SoftScratchBasedTest extends UsefulTestCase{

    protected Project myProject;
    protected VirtualFile myProjectRoot;
    protected String myProjectPath;

    protected Lesson myLesson;

    @org.junit.runners.Parameterized.Parameter(0) public String lessonId;

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

    @NotNull
    protected com.intellij.openapi.module.ModuleType getModuleType() {
        return StdModuleTypes.JAVA;
    }

    protected void setUpLesson(){
        myLesson = CourseManagerWithoutIDEA.getInstance().findLesson(lessonId);
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object> data(){
        List<Object> lessonsIds = new ArrayList<Object>();
        final Module[] modules = CourseManagerWithoutIDEA.getInstance().getModules();
        for (Module module : modules) {
            final ArrayList<Lesson> lessons = module.getLessons();
            for (Lesson lesson : lessons) {
                if (lesson.getModule().moduleType == Module.ModuleType.SCRATCH) {
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
            public void run() {

                try {
                    SoftScratchBasedTest.super.setUp();
                    myProjectFixture = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getTestName(true)).getFixture();
                    myProjectFixture.setUp();
                    myProject = myProjectFixture.getProject();
                    myProjectRoot = myProject.getBaseDir();
                    myProjectPath = myProjectRoot.getPath();
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
    public void tearDown ()throws Exception {
            final Ref<Exception> ex = new Ref<Exception>();
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    if(myProjectFixture != null) {
                        myProjectFixture.tearDown();
                    }
                } catch (Exception e) {
                    ex.set(e);
                } finally {
                    try {
                        SoftScratchBasedTest.super.tearDown();
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

                boolean noSdkDetected = false;
                try {
                    CourseManager.getInstance().checkEnvironment(myProject, myLesson.getModule());
                } catch (NoSdkException e) {
                    noSdkDetected = true;
                } catch (OldJdkException e) {
                    e.printStackTrace();
                } catch (InvalidSdkException e) {
                    e.printStackTrace();
                } catch (NoJavaModuleException e) {
                    e.printStackTrace();
                }
                assertTrue(noSdkDetected);
                final Boolean result = ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
                    @Override
                    public Boolean compute() {
                        ProjectRootManager.getInstance(myProject).setProjectSdk(getProjectJDK());
                        return true;
                    }
                });
                if (result) {
                    assertNotNull(ProjectJdkTable.getInstance().findJdk(getProjectJDK().getName(), getProjectJDK().getSdkType().getName()));
                    try {
                        CourseManager.getInstance().openLesson(myProject, myLesson);
                    } catch (BadModuleException e) {
                        e.printStackTrace();
                    } catch (BadLessonException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (FontFormatException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (LessonIsOpenedException e) {
                        e.printStackTrace();
                    }
                } else {
                    assertTrue(result); //Unable to add new
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
            } catch (BadCommandException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
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





    protected void prepareLesson(){
        assertNotNull(myLesson);
        myLesson.setPassed(false);
        assertTrue(!myLesson.getPassed());
    }


}
