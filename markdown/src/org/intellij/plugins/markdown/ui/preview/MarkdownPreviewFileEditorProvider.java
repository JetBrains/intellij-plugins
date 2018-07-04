package org.intellij.plugins.markdown.ui.preview;

import com.intellij.ide.scratch.ScratchFileType;
import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.intellij.plugins.markdown.lang.MarkdownLanguage;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor.SplitEditorLayout;
import org.jetbrains.annotations.NotNull;

public class MarkdownPreviewFileEditorProvider extends WeighedFileEditorProvider {
  @Override
  public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
    final FileType fileType = file.getFileType();

    if (!(fileType == MarkdownFileType.INSTANCE ||
          fileType == ScratchFileType.INSTANCE && LanguageUtil.getLanguageForPsi(project, file) == MarkdownLanguage.INSTANCE) ||
        !MarkdownHtmlPanelProvider.hasAvailableProviders()) {
      return false;
    }

    MarkdownSplitEditorProvider provider = FileEditorProvider.EP_FILE_EDITOR_PROVIDER.findExtension(MarkdownSplitEditorProvider.class);
    if (provider == null) {
      return false;
    }

    FileEditorState state = EditorHistoryManager.getInstance(project).getState(file, provider);
    if (!(state instanceof SplitFileEditor.MyFileEditorState)) {
      return true;
    }

    return SplitEditorLayout.valueOf(((SplitFileEditor.MyFileEditorState)state).getSplitLayout()) != SplitEditorLayout.FIRST;
  }

  @NotNull
  @Override
  public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
    return new MarkdownPreviewFileEditor(file);
  }

  @NotNull
  @Override
  public String getEditorTypeId() {
    return "markdown-preview-editor";
  }

  @NotNull
  @Override
  public FileEditorPolicy getPolicy() {
    return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
  }
}
