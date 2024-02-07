// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreviewProvider;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import org.jetbrains.annotations.NotNull;

final class JdlSplitEditorProvider extends TextEditorWithPreviewProvider {
  JdlSplitEditorProvider() {
    super(new PsiAwareTextEditorProvider(), new JdlPreviewFileEditorProvider());
  }

  @NotNull
  @Override
  protected FileEditor createSplitEditor(@NotNull FileEditor firstEditor, @NotNull FileEditor secondEditor) {
    return new JdlEditorWithPreview((TextEditor)firstEditor, (JdlPreviewFileEditor)secondEditor);
  }
}
