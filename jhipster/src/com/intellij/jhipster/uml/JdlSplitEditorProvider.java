// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

final class JdlSplitEditorProvider implements FileEditorProvider, DumbAware {
  private final FileEditorProvider myFirstProvider = new PsiAwareTextEditorProvider();
  private final FileEditorProvider mySecondProvider = new JdlPreviewFileEditorProvider();

  @Override
  public @NotNull @NonNls String getEditorTypeId() {
    return "jhipster-split-editor";
  }

  @Override
  public @NotNull FileEditorPolicy getPolicy() {
    return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
  }

  @Override
  public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
    return myFirstProvider.accept(project, file) && mySecondProvider.accept(project, file);
  }

  @Override
  public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
    return createEditorAsync(project, file).build();
  }

  @NotNull
  public AsyncFileEditorProvider.Builder createEditorAsync(@NotNull final Project project, @NotNull final VirtualFile file) {
    AsyncFileEditorProvider.Builder firstBuilder = getBuilderFromEditorProvider(myFirstProvider, project, file);
    AsyncFileEditorProvider.Builder secondBuilder = getBuilderFromEditorProvider(mySecondProvider, project, file);

    return new AsyncFileEditorProvider.Builder() {
      @Override
      public @NotNull FileEditor build() {
        return new JdlEditorWithPreview((TextEditor)firstBuilder.build(), (JdlPreviewFileEditor)secondBuilder.build());
      }
    };
  }

  @NotNull
  static AsyncFileEditorProvider.Builder getBuilderFromEditorProvider(@NotNull FileEditorProvider provider,
                                                                      @NotNull Project project,
                                                                      @NotNull VirtualFile file) {
    return new AsyncFileEditorProvider.Builder() {
      @Override
      public @NotNull FileEditor build() {
        return provider.createEditor(project, file);
      }
    };
  }
}
