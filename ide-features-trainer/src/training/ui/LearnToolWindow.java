package training.ui;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import training.editor.eduUI.EduPanel;
import training.editor.eduUI.MainEduPanel;
import training.learn.CourseManager;

import javax.swing.*;



/**
 * Created by karashevich on 17/03/16.
 */
public class LearnToolWindow extends SimpleToolWindowPanel implements DataProvider, Disposable {

    JPanel myContentPanel;
    EduPanel myEduPanel;
    MainEduPanel mainEduPanel;

    public LearnToolWindow() {
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

        myEduPanel = new EduPanel(preferableWidth);
        mainEduPanel = new MainEduPanel(preferableWidth);
        CourseManager.getInstance().setMainEduPanel(mainEduPanel);
        CourseManager.getInstance().setEduPanel(myEduPanel);
        setContent(mainEduPanel);
    }

    @Override
    public void dispose() {
        CourseManager.getInstance().setEduPanel(null);
        myEduPanel = null;
    }
}

