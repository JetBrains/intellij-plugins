package org.intellij.plugins.markdown.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAware;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;

public class ExpandSplitAction extends AnAction implements DumbAware {
  @Override
  public void update(AnActionEvent e) {
    final FileEditor editor = e.getData(PlatformDataKeys.FILE_EDITOR);
    e.getPresentation().setEnabled(editor instanceof SplitFileEditor
                                   || SplitFileEditor.PARENT_SPLIT_KEY.get(editor) != null);
  }

  @Override
  public boolean displayTextInToolbar() {
    return true;
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final FileEditor editor = e.getData(PlatformDataKeys.FILE_EDITOR);
    final SplitFileEditor splitFileEditor;
    if (editor instanceof SplitFileEditor) {
      splitFileEditor = (SplitFileEditor)editor;
    }
    else {
      splitFileEditor = SplitFileEditor.PARENT_SPLIT_KEY.get(editor);
    }

    if (splitFileEditor != null) {
      splitFileEditor.triggerLayoutChange();
    }
  }
}
