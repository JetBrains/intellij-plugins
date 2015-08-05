package org.jetbrains.training.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.training.util.LearnUiUtil;

/**
 * Created by karashevich on 28/07/15.
 */
public class ShowEditor extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
//        LearnUiUtil.getInstance().getEditorWindow(anActionEvent.getProject());
        LearnUiUtil.getInstance().highlightIdeComponent(LearnUiUtil.IdeComponent.EDITOR, anActionEvent.getProject());
    }
}
