// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.jhipster.JdlBundle;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import org.jetbrains.annotations.NotNull;

final class JdlEditorWithPreview extends TextEditorWithPreview {

  JdlEditorWithPreview(@NotNull TextEditor editor, @NotNull JdlPreviewFileEditor preview) {
    super(
      editor,
      preview,
      JdlBundle.message("jdl.preview"),
      Layout.SHOW_EDITOR_AND_PREVIEW,
      false
    );
    // todo subscribe to settings change
  }

  @Override
  protected @NotNull ActionGroup createViewActionGroup() {
    return new DefaultActionGroup(
      getShowEditorAction(),
      getShowEditorAndPreviewAction(),
      getShowPreviewAction(),
      ActionManager.getInstance().getAction("JDL.Generate")
    );
  }
}
