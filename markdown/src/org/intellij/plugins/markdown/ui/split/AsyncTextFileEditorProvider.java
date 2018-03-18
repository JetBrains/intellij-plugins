package org.intellij.plugins.markdown.ui.split;

import com.intellij.openapi.fileEditor.AsyncFileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public abstract class AsyncTextFileEditorProvider implements AsyncFileEditorProvider, DumbAware {
  @NotNull
  protected FileEditorProvider myProvider;
  @NotNull
  protected String myEditorTypeId;

  public AsyncTextFileEditorProvider(@NotNull FileEditorProvider provider) {
    myProvider = provider;
    myEditorTypeId = "provider[" + myProvider.getEditorTypeId() + "]";
  }

  @Override
  public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
    return myProvider.accept(project, file);
  }

  @NotNull
  @Override
  public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
    return createEditorAsync(project, file).build();
  }

  @NotNull
  @Override
  public String getEditorTypeId() {
    return myEditorTypeId;
  }

  @NotNull
  @Override
  public Builder createEditorAsync(@NotNull final Project project, @NotNull final VirtualFile file) {
    final Builder builder = MarkdownEditorUtil.getBuilderFromEditorProvider(myProvider, project, file);

    return new Builder() {
      @Override
      public FileEditor build() {
        return createFileEditor(builder.build());
      }
    };
  }

  protected abstract FileEditor createFileEditor(@NotNull FileEditor firstEditor);

  @NotNull
  @Override
  public FileEditorPolicy getPolicy() {
    return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
  }
}