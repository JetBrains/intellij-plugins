package training.editor.eduUI;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.ui.JBColor;
import training.editor.EduEditor;

import java.awt.*;

/**
 * Created by karashevich on 26/10/15.
 */
public class DemoModeUI {

    final private static JBColor demoCurtainColor = new JBColor(new Color(42, 42, 42, 26), new Color(128, 128, 128, 26));
    private DemoModeWidget demoModeWidget = null;
    private Project myProject = null;

    public static JBColor getDemoCurtainColor() {
        return demoCurtainColor;
    }

    public void addDemoModeWidget(Project project, EduEditor eduEditor) {

        myProject = project;

        final IdeFrameImpl frame = WindowManagerEx.getInstanceEx().getFrame(project);
        final StatusBar statusBar = frame.getStatusBar();

        if (statusBar != null) {
            if (statusBar.getWidget(DemoModeWidget.DEMO_MODE_WIDGET_ID) != null) {
                demoModeWidget = (DemoModeWidget) statusBar.getWidget(DemoModeWidget.DEMO_MODE_WIDGET_ID);
            } else {
                if (demoModeWidget == null) {
                    demoModeWidget = new DemoModeWidget(project);
                    statusBar.addWidget(demoModeWidget, "before Position", eduEditor);
                } else {
                    statusBar.addWidget(demoModeWidget, "before Position", eduEditor);
                }
            }
            statusBar.updateWidget(demoModeWidget.ID());
        }
    }

    public void updateDemoModeWidget() {
        if (myProject == null) return;

        final IdeFrameImpl frame = WindowManagerEx.getInstanceEx().getFrame(myProject);
        final StatusBar statusBar = frame.getStatusBar();

        statusBar.updateWidget(demoModeWidget.ID());
    }

    public void removeDemoModeWidget() {
        final IdeFrameImpl frame = WindowManagerEx.getInstanceEx().getFrame(myProject);
        final StatusBar statusBar = frame.getStatusBar();

        statusBar.removeWidget(demoModeWidget.ID());
        demoModeWidget = null;
    }
}