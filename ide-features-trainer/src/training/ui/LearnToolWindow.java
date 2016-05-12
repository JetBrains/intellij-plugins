package training.ui;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.impl.StripeButton;
import com.intellij.openapi.wm.impl.ToolWindowsPane;
import com.intellij.ui.GotItMessage;
import com.intellij.ui.UIBundle;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.Alarm;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;
import training.learn.CourseManager;
import training.learn.LearnBundle;

import javax.swing.*;
import java.awt.*;


/**
 * Created by karashevich on 17/03/16.
 */
public class LearnToolWindow extends SimpleToolWindowPanel implements DataProvider, Disposable {

    private JPanel myContentPanel;
    private JBScrollPane scrollPane;
    private LearnPanel myLearnPanel;
    private MainLearnPanel mainLearnPanel;
    private Project myProject;

    LearnToolWindow() {
        super(true, true);
        myContentPanel = new JPanel();
    }


    public LearnToolWindow(boolean vertical) {
        super(vertical);
    }

    public LearnToolWindow(boolean vertical, boolean borderless) {
        super(vertical, borderless);
    }

    public void init(Project project) {
        int preferableWidth = 350;

        myProject = project;

        myLearnPanel = new LearnPanel(preferableWidth);
        mainLearnPanel = new MainLearnPanel(preferableWidth);
        CourseManager.getInstance().setMainLearnPanel(mainLearnPanel);
        CourseManager.getInstance().setLearnPanel(myLearnPanel);
        scrollPane = new JBScrollPane(mainLearnPanel);
        setContent(scrollPane);
    }

    public JBScrollPane getScrollPane() {
        return scrollPane;
    }

    @Override
    public void dispose() {
        CourseManager.getInstance().setLearnPanel(null);
        myLearnPanel = null;
    }



}

