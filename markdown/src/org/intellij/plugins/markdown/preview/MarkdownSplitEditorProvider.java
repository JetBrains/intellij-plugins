package org.intellij.plugins.markdown.preview;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import org.intellij.plugins.markdown.preview.split.SplitFileEditor;
import org.intellij.plugins.markdown.preview.split.SplitTextEditorProvider;
import org.jetbrains.annotations.NotNull;

public class MarkdownSplitEditorProvider extends SplitTextEditorProvider {
  public MarkdownSplitEditorProvider() {
    super(new PsiAwareTextEditorProvider(), new MarkdownPreviewFileEditorProvider());
  }

  @Override
  protected FileEditor createSplitEditor(@NotNull final FileEditor firstEditor, @NotNull FileEditor secondEditor) {
    return new SplitFileEditor(firstEditor, secondEditor) {
      @NotNull
      @Override
      public String getName() {
        return "Markdown split editor";
      }

    };
  }

}
