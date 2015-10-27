package training.editor.eduUI;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.ui.JBColor;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.editor.EduEditor;
import training.lesson.EducationBundle;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by karashevich on 26/10/15.
 */
public class DemoModeUI {

    final private static JBColor demoCurtainColor = new JBColor(new Color(42, 42, 42, 26), new Color(128, 128, 128, 26));
    private DemoModeWidget demoModeWidget = null;

    public static JBColor getDemoCurtainColor() {
        return demoCurtainColor;
    }

    public void addDemoModeWidget(Project project, EduEditor eduEditor) {
        final IdeFrameImpl frame = WindowManagerEx.getInstanceEx().getFrame(project);
        final StatusBar statusBar = frame.getStatusBar();

        if (statusBar != null) {
            if (statusBar.getWidget(DemoModeWidget.DEMO_MODE_WIDGET_ID) != null) {
                demoModeWidget = (DemoModeWidget) statusBar.getWidget(DemoModeWidget.DEMO_MODE_WIDGET_ID);
            } else {
                if (demoModeWidget == null) {
                    demoModeWidget = new DemoModeWidget(project);
                    statusBar.addWidget(demoModeWidget, "before Position", eduEditor);
                    statusBar.updateWidget(demoModeWidget.ID());
                } else {
                    statusBar.addWidget(demoModeWidget, "before Position", eduEditor);
                    statusBar.updateWidget(demoModeWidget.ID());
                }
            }
        }
    }
}