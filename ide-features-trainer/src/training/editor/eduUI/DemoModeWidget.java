package training.editor.eduUI;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
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
public class DemoModeWidget extends EditorBasedWidget implements StatusBarWidget.Multiframe, StatusBarWidget.TextPresentation, CaretListener, SelectionListener {

    StatusBar myStatusBar;
    FileEditor currentEditor = null;

    final public static String DEMO_MODE_WIDGET_ID = "DemoMode";

    protected DemoModeWidget(@NotNull Project project) {
        super(project);
        final FileEditor[] selectedEditors = FileEditorManager.getInstance(project).getSelectedEditors();

        for (FileEditor selectedEditor : selectedEditors) {
            if (selectedEditor instanceof EduEditor) currentEditor = selectedEditor;
        }

        if (currentEditor == null) currentEditor = selectedEditors[0];
    }


    @Override
    public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
        myStatusBar = statusBar;
        final EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
        multicaster.addCaretListener(this, this);
        multicaster.addSelectionListener(this, this);
        myStatusBar.updateWidget(ID());
    }

    @Override
    public void selectionChanged(SelectionEvent e) {
        if (e.getEditor() instanceof FileEditor) {
            currentEditor = (FileEditor) e.getEditor();
        } else {
            currentEditor = null;
        }
        myStatusBar.updateWidget(ID());

    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        currentEditor = event.getNewEditor();
        myStatusBar.updateWidget(ID());    }

    @NotNull
    @Override
    public String ID() {
        return DEMO_MODE_WIDGET_ID;
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation(@NotNull PlatformType type) {
        return this;
    }

    @Override
    public StatusBarWidget copy() {
        return null;
    }

    @NotNull
    @Override
    public String getText() {

        if((currentEditor != null) && (currentEditor instanceof EduEditor)) {
            if(((EduEditor) currentEditor).isDemoModeOn()) {
                return "DEMO MODE ON";
            } else {
                return "";
            }
        }
        return "";
    }

    @NotNull
    @Override
    public String getMaxPossibleText() {
        return "DEMO MODE ON";
    }

    @Override
    public float getAlignment() {
        return Component.CENTER_ALIGNMENT;
    }

    @Nullable
    @Override
    public String getTooltipText() {
        return EducationBundle.message("status.demoMode.tooltipText");
    }

    @Nullable
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    @Override
    public void caretPositionChanged(CaretEvent e) {

    }

    @Override
    public void caretAdded(CaretEvent e) {

    }

    @Override
    public void caretRemoved(CaretEvent e) {

    }
}