package org.intellij.plugins.markdown.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;

public class ExpandSplitAction extends AnAction implements DumbAware {
  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().setEnabled(MarkdownActionUtil.findSplitEditor(e) != null);
  }

  @Override
  public boolean displayTextInToolbar() {
    return true;
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final SplitFileEditor splitFileEditor = MarkdownActionUtil.findSplitEditor(e);

    if (splitFileEditor != null) {
      splitFileEditor.triggerLayoutChange();
    }
  }
}
