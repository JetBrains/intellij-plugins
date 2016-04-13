package training.component;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import training.learn.CourseManager;
import training.ui.LearnToolWindowFactory;

/**
 * Created by karashevich on 17/03/16.
 */
public class LearnProjectComponent implements ProjectComponent {
    private static final Logger LOG = Logger.getInstance(LearnProjectComponent.class.getName());
    private final Project myProject;

    private LearnProjectComponent(@NotNull Project project){
        myProject = project;
    }



    @Override
    public void projectOpened() {
        registerLearnToolWindow(myProject);
        CourseManager.getInstance().updateToolWindow(myProject);
    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "LearnProjectComponent";
    }

    private void registerLearnToolWindow(@NotNull Project project) {

        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);

        //register tool window
        final ToolWindow toolWindow = toolWindowManager.getToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW);
        if (toolWindow == null) {
            toolWindowManager.registerToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW, true, ToolWindowAnchor.LEFT, myProject, true);
        }

        //(if commented) do not always show learn tool window after idea has started
//        final ToolWindow learnToolWindow = toolWindowManager.getToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW);
//        if (learnToolWindow != null) {
//            learnToolWindow.show(null);
//        }
    }
}
