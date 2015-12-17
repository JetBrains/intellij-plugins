import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.impl.ComponentManagerImpl;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.impl.EditorFactoryImpl;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.impl.VirtualFilePointerManagerImpl;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.HeavyIdeaTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.ui.EdtInvocationManager;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import training.editor.EduEditor;
import training.editor.EduEditorFactory;
import training.editor.EduEditorManager;
import training.editor.EduEditorProvider;
import training.lesson.*;
import training.lesson.exceptons.*;
import training.testFramework.LessonSolution;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karashevich on 28/10/15.
 */
@RunWith(Parameterized.class)
public class HardScratchBasedTest extends UsefulTestCase{

    protected Project myProject;
    protected VirtualFile myProjectRoot;
    protected String myProjectPath;
    protected LessonSolution myLessonSolution;

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

    @NotNull
    protected ModuleType getModuleType() {
        return StdModuleTypes.JAVA;
    }

    protected void setUpLesson(){
        myLesson = CourseManagerWithoutIDEA.getInstance().findLesson(lessonId);
    }

    private void setUpSolution() throws Exception {
        myLessonSolution = CourseManagerWithoutIDEA.getInstance().findSolution(lessonId);
        assertNotNull(myLessonSolution);
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object> data(){
        List<Object> lessonsIds = new ArrayList<Object>();
        final Course[] courses = CourseManagerWithoutIDEA.getInstance().getCourses();
        for (Course course : courses) {
            final ArrayList<Lesson> lessons = course.getLessons();
            for (Lesson lesson : lessons) {
                if (lesson.getCourse().courseType == Course.CourseType.SCRATCH) {
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
                    HardScratchBasedTest.super.setUp();


                    myProjectFixture = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getTestName(true)).getFixture();

                    myProjectFixture.setUp();

                    myProject = myProjectFixture.getProject();
                    myProjectRoot = myProject.getBaseDir();
                    myProjectPath = myProjectRoot.getPath();

                    //Swap EditorFactoryClasses with EduEditorFactory
                    final EduEditorFactory eduEditorFactory = new EduEditorFactory(ProjectManager.getInstance());
                    eduEditorFactory.cloneEventMulticaster(myProject);
                    final Class<EditorFactory> editorFactoryClass = EditorFactory.class;
                    final ComponentManagerImpl componentManager = (ComponentManagerImpl) ApplicationManager.getApplication();
//                    eduEditorFactory[0].cloneEventMulticaster(myProject);
                    componentManager.registerComponentInstance(editorFactoryClass, eduEditorFactory);

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
                    final ComponentManagerImpl componentManager = (ComponentManagerImpl) ApplicationManager.getApplication();

                    disposeAllEduEditors();
                    if(myProjectFixture != null) {
                        myProjectFixture.tearDown();
                    }
                } catch (Exception e) {
                    ex.set(e);
                } finally {
                    try {
                        HardScratchBasedTest.super.tearDown();
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
        setUpSolution();
        prepareLesson();
        Runnable runnable = new Runnable() {
            public void run() {
                ((VirtualFilePointerManagerImpl) VirtualFilePointerManager.getInstance()).storePointers();

                boolean noSdkDetected = false;
                try {
                    CourseManager.getInstance().checkEnvironment(myProject, myLesson.getCourse());
                } catch (NoSdkException e) {
                    noSdkDetected = true;
                } catch (OldJdkException e) {
                    e.printStackTrace();
                } catch (InvalidSdkException e) {
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
                    } catch (Exception e) {
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
            if (myLesson.getPassed()) break;
            EdtTestUtil.runInEdtAndWait(new ThrowableRunnable<Throwable>() {
                @Override
                public void run() {
                    try {
                        System.out.println(lessonId + ": solving step " + (LessonProcessor.getCurrentExecutionList().getElements().size() - 1));
                        myLessonSolution.solveStep();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    private void disposeAllEduEditors() {
        final EduEditor[] allNotDisposedEduEditors = EduEditorManager.getInstance().getAllNotDisposedEduEditors();
        for (int i = 0; i < allNotDisposedEduEditors.length; i++) {
            EduEditorManager.getInstance().clearMap();
        }
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
