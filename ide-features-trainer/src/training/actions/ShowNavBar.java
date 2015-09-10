package training.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import training.util.LearnUiUtil;

/**
 * Created by karashevich on 28/07/15.
 */
public class ShowNavBar extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
//        LearnUiUtil.getInstance().getEditorWindow(anActionEvent.getProject());
        LearnUiUtil.getInstance().highlightIdeComponent(LearnUiUtil.IdeComponent.NAVIGATION_BAR, anActionEvent.getProject());
    }
}
