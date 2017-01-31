package training.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.ToolWindowManager;
import training.ui.LearnToolWindowFactory;
import training.ui.views.LearnPanel;

import java.awt.*;

/**
 * Created by jetbrains on 29/07/16.
 *
 *  Skip lesson or go to the next lesson
 *
 *  Shortcuts:
 *      win: alt + shift + right
 *      mac: ctrl + shift + right
 *      (see the plugin.xml to change shortcuts)
 */
public class NextLessonAction extends AnAction{

    @Override
    public void actionPerformed(AnActionEvent e) {
        //check if the lesson view is activate
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(e.getProject());
        Component view = LearnToolWindowFactory.getMyLearnToolWindow().getScrollPane().getViewport().getView();

        if (view instanceof LearnPanel) {
            //click button to skip or go to the next lesson
            LearnPanel learnPanel = (LearnPanel) view;
            learnPanel.clickButton();
        }
    }
}
