package training.component;

import com.intellij.ide.ui.UISettings;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.*;
import com.intellij.openapi.wm.impl.IdeRootPane;
import com.intellij.openapi.wm.impl.StripeButton;
import com.intellij.openapi.wm.impl.ToolWindowsPane;
import com.intellij.ui.GotItMessage;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Alarm;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.learn.CourseManager;
import training.learn.LearnBundle;
import training.ui.LearnToolWindow;
import training.ui.LearnToolWindowFactory;

import javax.swing.*;
import java.awt.*;

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

        StartupManager.getInstance(myProject).registerPostStartupActivity(new Runnable() {
            @Override
            public void run() {
                pluginFirstStart();
            }
        });

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
    }

    public void pluginFirstStart(){
        final String key = "learn.toolwindow.button.info.shown";
        //TODO: remove before release
        PropertiesComponent.getInstance().setValue(key, String.valueOf(false));
        if (UISettings.getInstance().HIDE_TOOL_STRIPES) {
            UISettings.getInstance().HIDE_TOOL_STRIPES = false;
            UISettings.getInstance().fireUISettingsChanged();
        }

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                final StripeButton learnStripeButton = getLearnStripeButton();
                if (learnStripeButton == null) return;

                if (!PropertiesComponent.getInstance().isTrueValue(key)) {
                    PropertiesComponent.getInstance().setValue(key, String.valueOf(true));
                    final Alarm alarm = new Alarm();
                    alarm.addRequest(new Runnable() {
                        @Override
                        public void run() {
                            GotItMessage.createMessage(LearnBundle.message("learn.tool.window.quick.access.title"), LearnBundle.message("learn.tool.window.quick.access.message"))
                                    .setDisposable(myProject)
                                    .show(new RelativePoint(learnStripeButton, new Point(learnStripeButton.getBounds().width, learnStripeButton.getBounds().height/2)), Balloon.Position.atRight);
                            Disposer.dispose(alarm);
                        }
                    }, 10000);
                }
            }
        });
    }

    @Nullable
    private StripeButton getLearnStripeButton(){

        StripeButton learnStripeButton = null;
        final JRootPane rootPane = ((JFrame) WindowManager.getInstance().getIdeFrame(myProject)).getRootPane();
        ToolWindowsPane pane = UIUtil.findComponentOfType(rootPane, ToolWindowsPane.class);
        final java.util.List<StripeButton> componentsOfType = UIUtil.findComponentsOfType(pane, StripeButton.class);
        for (StripeButton stripeButton : componentsOfType) {
            if (stripeButton.getText().equals(LearnToolWindowFactory.LEARN_TOOL_WINDOW))
                learnStripeButton = stripeButton;
        }

        return learnStripeButton;
    }

}
