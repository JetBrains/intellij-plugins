package org.intellij.plugins.markdown.ui.preview;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretAdapter;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.fileEditor.TextEditor;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;

public class MarkdownSplitEditor extends SplitFileEditor {
  public MarkdownSplitEditor(@NotNull TextEditor mainEditor,
                             @NotNull MarkdownPreviewFileEditor secondEditor) {
    super(mainEditor, secondEditor);

    mainEditor.getEditor().getCaretModel().addCaretListener(new MyCaretListener(secondEditor));
  }

  @NotNull
  @Override
  public String getName() {
    return "Markdown split editor";
  }

  @NotNull
  @Override
  protected AnAction[] createToolbarActions() {
    return new AnAction[]{
      ActionManager.getInstance().getAction("org.intellij.plugins.markdown.ui.actions.ToggleItalicAction"),
      ActionManager.getInstance().getAction("org.intellij.plugins.markdown.ui.actions.ToggleBoldAction"),
      ActionManager.getInstance().getAction("org.intellij.plugins.markdown.ui.actions.ToggleCodeSpanAction")
    };
  }

  private static class MyCaretListener extends CaretAdapter {
    @NotNull
    private final MarkdownPreviewFileEditor myPreviewFileEditor;

    public MyCaretListener(@NotNull MarkdownPreviewFileEditor previewFileEditor) {
      myPreviewFileEditor = previewFileEditor;
    }

    @Override
    public void caretPositionChanged(CaretEvent e) {
      final Editor editor = e.getEditor();
      if (editor.getCaretModel().getCaretCount() != 1) {
        return;
      }

      final int offset = editor.logicalPositionToOffset(e.getNewPosition());
      myPreviewFileEditor.scrollToSrcOffset(offset);
    }
  }
}
