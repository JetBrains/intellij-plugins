package training.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.wm.IdeFocusManager;
import training.learn.CourseManager;
import training.learn.LearnBundle;

import java.awt.*;

/**
 * Created by jetbrains on 15/07/16.
 */
public class StartLearnAction extends AnAction {


    public StartLearnAction() {
        super(LearnBundle.message("learn.WelcomeScreen.StartLearn.text", ApplicationNamesInfo.getInstance().getFullProductName()), LearnBundle.message("learn.WelcomeScreen.StartLearn.description"), AllIcons.Actions.Execute);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final AnAction action = ActionManager.getInstance().getAction("learn.open.lesson");

        final DataContext context = DataContext.EMPTY_CONTEXT;
        final AnActionEvent event = AnActionEvent.createFromAnAction(action, null, "", context);

        ActionUtil.performActionDumbAware(action, event);

    }

}