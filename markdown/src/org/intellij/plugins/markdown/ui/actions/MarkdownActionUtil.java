package org.intellij.plugins.markdown.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
import org.jetbrains.annotations.Nullable;

class MarkdownActionUtil {
  @Nullable
  public static SplitFileEditor findSplitEditor(AnActionEvent e) {
    final FileEditor editor = e.getData(PlatformDataKeys.FILE_EDITOR);
    if (editor instanceof SplitFileEditor) {
      return (SplitFileEditor)editor;
    }
    else {
      return SplitFileEditor.PARENT_SPLIT_KEY.get(editor);
    }
  }
}
