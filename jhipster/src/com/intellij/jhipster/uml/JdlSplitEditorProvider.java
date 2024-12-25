// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreviewProvider;
import org.jetbrains.annotations.NotNull;

final class JdlSplitEditorProvider extends TextEditorWithPreviewProvider {
  JdlSplitEditorProvider() {
    super(new JdlPreviewFileEditorProvider());
  }

  @Override
  protected @NotNull FileEditor createSplitEditor(@NotNull TextEditor firstEditor, @NotNull FileEditor secondEditor) {
    return new JdlEditorWithPreview(firstEditor, (JdlPreviewFileEditor)secondEditor);
  }
}
