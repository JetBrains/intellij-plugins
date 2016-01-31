package training.lesson.dialogs;

import com.intellij.CommonBundle;
import com.intellij.ide.IdeBundle;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.lesson.EducationBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by karashevich on 15/01/16.
 */
public class LessonDialog extends DialogWrapper {
    private LessonDialogPanel myLessonDialogPanel;

    public LessonDialog(){
        super(WindowManagerEx.getInstanceEx().findVisibleFrame(), true);
        initialize();
    }

    public LessonDialog(@NotNull final Window parent) {
        super(parent, true);
        initialize();
    }

    private void initialize() {
        setModal(false);
        setTitle(EducationBundle.message("dialog.lessonDialog.title"));
        setCancelButtonText("&Ok");
        myLessonDialogPanel = new LessonDialogPanel();
        setHorizontalStretch(1.33f);
        setVerticalStretch(1.25f);
        init();
    }

    public static LessonDialog createForProject(final Project project) {
        final Window w = WindowManagerEx.getInstanceEx().suggestParentWindow(project);
        return (w == null) ? new LessonDialog() : new LessonDialog(w);
    }

    public void setContent(String fileName){
        myLessonDialogPanel.setContent(fileName);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myLessonDialogPanel;
    }

    protected Action[] createActions(){
        return new Action[]{getCancelAction()};
    }

    public void dispose(){
        super.dispose();
    }


    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return myPreferredFocusedComponent;
    }
}
