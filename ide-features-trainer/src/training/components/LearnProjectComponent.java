package training.components;

import com.intellij.ide.ui.UISettings;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.*;
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
import training.ui.LearnIcons;
import training.ui.LearnToolWindowFactory;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by karashevich on 17/03/16.
 */
public class LearnProjectComponent implements ProjectComponent {
    private static final Logger LOG = Logger.getInstance(LearnProjectComponent.class.getName());
    private final Project myProject;

    private final static String SHOW_TOOLWINDOW_INFO = "learn.toolwindow.button.info.shown";


    private LearnProjectComponent(@NotNull Project project) {
        myProject = project;
    }


    @Override
    public void projectOpened() {
        registerLearnToolWindow(myProject);
//        startTrackActivity(myProject);
        CourseManager.getInstance().updateToolWindow(myProject);

        //show where learn tool window locates only on the first start
        if (!PropertiesComponent.getInstance().isTrueValue(SHOW_TOOLWINDOW_INFO)) {
            StartupManager.getInstance(myProject).registerPostStartupActivity(new Runnable() {
                @Override
                public void run() {
                    pluginFirstStart();
                }
            });
        }

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
            ToolWindow createdToolWindow = toolWindowManager.registerToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW, true, ToolWindowAnchor.LEFT, myProject, true);
            createdToolWindow.setIcon(LearnIcons.ChevronToolWindowIcon);
        }
    }

    public void startTrackActivity(Project project) {
        final Alarm alarm = new Alarm();
        if (CourseManager.getInstance().getLastActivityTime() == -1) return;
        if (CourseManager.getInstance().calcUnpassedLessons() == 0) return;
        if (CourseManager.getInstance().calcPassedLessons() == 0) return;
        alarm.addRequest(new Runnable() {
            @Override
            public void run() {
                long lastActivityTime = CourseManager.getInstance().getLastActivityTime();
                long currentTimeMillis = System.currentTimeMillis();
                final long TWO_WEEKS = TimeUnit.DAYS.toMillis(14);

                if (currentTimeMillis - lastActivityTime > TWO_WEEKS) {
                    StringBuilder message = new StringBuilder();
                    final int unpassedLessons = CourseManager.getInstance().calcUnpassedLessons();

                    message.append(LearnBundle.message("learn.activity.message", unpassedLessons, unpassedLessons == 1 ? "" : LearnBundle.message("learn.activity.message.lessons"))).append("<br/>");
                    final Notification notification = new Notification(CourseManager.NOTIFICATION_ID,
                            LearnBundle.message("learn.activity.title"),
                            message.toString(),
                            NotificationType.INFORMATION);
                    AnAction learnAction = new AnAction(LearnBundle.message("learn.activity.learn")) {
                        @Override
                        public void actionPerformed(AnActionEvent e) {
                            final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW);
                            toolWindow.activate(null);
                            notification.expire();
                        }
                    };
                    AnAction laterAction = new AnAction(LearnBundle.message("learn.activity.later")) {
                        @Override
                        public void actionPerformed(AnActionEvent e) {
                            notification.expire();
                        }
                    };
                    AnAction neverAction = new AnAction(LearnBundle.message("learn.activity.never")) {
                        @Override
                        public void actionPerformed(AnActionEvent e) {
                            CourseManager.getInstance().setLastActivityTime(-1);
                            notification.expire();
                        }
                    };
                    notification.addAction(learnAction).addAction(laterAction).addAction(neverAction).notify(project);
                }
                Disposer.dispose(alarm);
            }
        }, 30000);
    }

    private void pluginFirstStart() {

        //do not show popups in test mode
        if (ApplicationManager.getApplication().isUnitTestMode()) return;

        if (UISettings.getInstance().HIDE_TOOL_STRIPES) {
            UISettings.getInstance().HIDE_TOOL_STRIPES = false;
            UISettings.getInstance().fireUISettingsChanged();
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            final StripeButton learnStripeButton = getLearnStripeButton();
            if (learnStripeButton == null) return;

            PropertiesComponent.getInstance().setValue(SHOW_TOOLWINDOW_INFO, String.valueOf(true));
            final Alarm alarm = new Alarm();
            alarm.addRequest(() -> {
                GotItMessage.createMessage(LearnBundle.message("learn.tool.window.quick.access.title"), LearnBundle.message("learn.tool.window.quick.access.message"))
                        .show(new RelativePoint(learnStripeButton, new Point(learnStripeButton.getBounds().width, learnStripeButton.getBounds().height / 2)), Balloon.Position.atRight);
                Disposer.dispose(alarm);
            }, 5000);
        });
    }

    @Nullable
    private StripeButton getLearnStripeButton() {

        StripeButton learnStripeButton = null;
        WindowManager wm = WindowManager.getInstance();
        if (wm == null) return null;
        IdeFrame ideFrame = wm.getIdeFrame(myProject);
        if (ideFrame == null) return null;

        final JRootPane rootPane = ((JFrame) ideFrame).getRootPane();
        ToolWindowsPane pane = UIUtil.findComponentOfType(rootPane, ToolWindowsPane.class);
        final java.util.List<StripeButton> componentsOfType = UIUtil.findComponentsOfType(pane, StripeButton.class);
        for (StripeButton stripeButton : componentsOfType) {
            if (stripeButton.getText().equals(LearnToolWindowFactory.LEARN_TOOL_WINDOW))
                learnStripeButton = stripeButton;
        }

        return learnStripeButton;
    }

}
