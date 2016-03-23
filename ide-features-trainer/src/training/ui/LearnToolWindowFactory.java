package training.ui;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import training.learn.CourseManager;

/**
 * Created by jetbrains on 17/03/16.
 */
public class LearnToolWindowFactory implements ToolWindowFactory, DumbAware {
    public static final String LEARN_TOOL_WINDOW = "Learn IntelliJ IDEA";


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        LearnToolWindow learnToolWindow = new LearnToolWindow();
        learnToolWindow.init(project);
        final ContentManager contentManager = toolWindow.getContentManager();

        Content content = contentManager.getFactory().createContent(learnToolWindow, null, false);
        contentManager.addContent(content);

        Disposer.register(project, learnToolWindow);
    }
}

