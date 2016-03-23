import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.impl.VirtualFilePointerManagerImpl;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import training.commands.Command;
import training.commands.ExecutionList;
import training.commands.TestCommand;
import training.learn.CourseManager;
import training.learn.LessonProcessor;
import training.learn.exceptons.NoSdkException;
import training.testFramework.EduLessonTest;

/**
 * Created by karashevich on 29/10/15.
 */
public class EditorBasics_Selection_Solution_Test extends EduLessonTest {


    /**
     *
     * stepNumber -- steps could be found from LessonProcessor.getCurrentExecutionList().elements. Steps are counting in reverse order. Here an example:
     * LessonProcessor.getCurrentExecutionList() = {training.commands.ExecutionList@7705}
     * elements = {java.util.concurrent.LinkedBlockingQueue@7706}  size = 6
     *                   0 = {org.jdom.Element@7715} "[Element: <Try/>]"    -> to solve this step use stepNumber = 5
     *                   1 = {org.jdom.Element@7716} "[Element: <Try/>]"    -> to solve this step use stepNumber = 4
     *                   2 = {org.jdom.Element@7717} "[Element: <Text/>]"   -> no need to solve, stepNumber = 3
     *                   3 = {org.jdom.Element@7718} "[Element: <Try/>]"    -> to solve this step use stepNumber = 2
     *                   4 = {org.jdom.Element@7719} "[Element: <Try/>]"    -> to solve this step use stepNumber = 1
     *                   5 = {org.jdom.Element@7720} "[Element: <Win/>]"    -> no need to solve, stepNumber = 0
     */
    @Override
    public void solveStep() throws Exception {
        final ExecutionList currentExecutionList = LessonProcessor.getCurrentExecutionList();
        if (currentExecutionList == null) return;
        int stepNumber = currentExecutionList.getElements().size() - 1;
        if (stepNumber == 5) {
            currentExecutionList.getElements().poll();
            Command testCommand = new TestCommand();
            testCommand.execute(currentExecutionList);
        }
        if (stepNumber == 4){
            currentExecutionList.getElements().poll();
            Command testCommand = new TestCommand();
            testCommand.execute(currentExecutionList);
        }
        if (stepNumber == 2){
            currentExecutionList.getElements().poll();
            Command testCommand = new TestCommand();
            testCommand.execute(currentExecutionList);
        }
        if (stepNumber == 1){
            currentExecutionList.getElements().poll();
            Command testCommand = new TestCommand();
            testCommand.execute(currentExecutionList);
        }

    }


    public void testLesson() throws Exception{
        ((VirtualFilePointerManagerImpl) VirtualFilePointerManager.getInstance()).storePointers();

        boolean noSdkDetected = false;
        try {
            CourseManager.getInstance().checkEnvironment(myProject, myLesson.getModule());
        } catch (NoSdkException e) {
            noSdkDetected = true;
        }
        assertTrue(noSdkDetected);
        noSdkDetected = false;
        final Boolean result = ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
            @Override
            public Boolean compute() {
                ProjectRootManager.getInstance(myProject).setProjectSdk(getProjectJDK());
                return true;
            }
        });
        if (result) {
            assertNotNull(ProjectJdkTable.getInstance().findJdk(getProjectJDK().getName(), getProjectJDK().getSdkType().getName()));
            CourseManager.getInstance().openLesson(myProject, myLesson);
        } else {
            assertTrue(result); //Unable to add new
        }
    }

    @Override
    protected void setUpLesson() {
        myLesson = CourseManager.getInstance().findLesson("Selection");
    }
}
