package org.jetbrains.training.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.training.util.LearnUiUtil;
import sun.plugin.util.UIUtil;

/**
 * Created by karashevich on 28/07/15.
 */
public class GetEditorWindow extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        LearnUiUtil.getInstance().getEditorWindow(anActionEvent.getProject());
    }
}
