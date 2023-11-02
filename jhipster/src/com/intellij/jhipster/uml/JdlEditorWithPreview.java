// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.jhipster.JdlBundle;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

final class JdlEditorWithPreview extends TextEditorWithPreview {
  public static final Key<JdlEditorWithPreview> PARENT_SPLIT_EDITOR_KEY = Key.create("jdl.parentSplit");

  JdlEditorWithPreview(@NotNull TextEditor editor, @NotNull JdlPreviewFileEditor preview) {
    super(
      editor,
      preview,
      JdlBundle.message("jdl.preview"),
      Layout.SHOW_EDITOR_AND_PREVIEW,
      false
    );

    editor.putUserData(PARENT_SPLIT_EDITOR_KEY, this);
    preview.putUserData(PARENT_SPLIT_EDITOR_KEY, this);

    preview.setMainEditor(editor.getEditor());

    // todo subscribe to settings change
  }

  @Override
  protected void onLayoutChange(Layout oldValue, Layout newValue) {
    super.onLayoutChange(oldValue, newValue);
    // Editor tab will lose focus after switching to JCEF preview for some reason.
    // So we should explicitly request focus for our editor here.
    if (newValue == Layout.SHOW_PREVIEW) {
      requestFocusForPreview();
    }
  }

  private void requestFocusForPreview() {
    final var preferredComponent = myPreview.getPreferredFocusedComponent();
    if (preferredComponent != null) {
      preferredComponent.requestFocus();
      return;
    }
    myPreview.getComponent().requestFocus();
  }

  @Override
  public void setLayout(@NotNull Layout layout) {
    super.setLayout(layout);
  }

  @Override
  public void setState(@NotNull FileEditorState state) {
    if (state instanceof JdlEditorWithPreviewState actualState) {
      super.setState(actualState.getUnderlyingState());
    }
  }

  @Override
  public @NotNull FileEditorState getState(@NotNull FileEditorStateLevel level) {
    final var underlyingState = super.getState(level);
    return new JdlEditorWithPreviewState(underlyingState);
  }

  @Override
  protected @NotNull ToggleAction getShowEditorAction() {
    return (ToggleAction)Objects.requireNonNull(ActionManager.getInstance().getAction("JDL.Layout.EditorOnly"));
  }

  @Override
  protected @NotNull ToggleAction getShowEditorAndPreviewAction() {
    return (ToggleAction)Objects.requireNonNull(ActionManager.getInstance().getAction("JDL.Layout.EditorAndPreview"));
  }

  @Override
  protected @NotNull ToggleAction getShowPreviewAction() {
    return (ToggleAction)Objects.requireNonNull(ActionManager.getInstance().getAction("JDL.Layout.PreviewOnly"));
  }

  static @Nullable JdlEditorWithPreview findSplitEditor(AnActionEvent event) {
    FileEditor fileEditor = event.getData(PlatformCoreDataKeys.FILE_EDITOR);
    if (fileEditor == null) return null;

    if (fileEditor instanceof JdlEditorWithPreview) {
      return (JdlEditorWithPreview)fileEditor;
    }

    return fileEditor.getUserData(PARENT_SPLIT_EDITOR_KEY);
  }

  @Override
  @NotNull
  protected ActionGroup createViewActionGroup() {
    return new DefaultActionGroup(
      getShowEditorAction(),
      getShowEditorAndPreviewAction(),
      getShowPreviewAction(),
      ActionManager.getInstance().getAction("JDL.Generate")
    );
  }
}
