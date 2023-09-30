// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import static com.intellij.jhipster.uml.JdlEditorWithPreview.findSplitEditor;

abstract class JdlChangePreviewLayoutAction extends ToggleAction implements DumbAware {

  private final TextEditorWithPreview.Layout layout;

  JdlChangePreviewLayoutAction(TextEditorWithPreview.Layout layout) {
    super(layout.getName(), layout.getName(), layout.getIcon(null));
    this.layout = layout;
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.EDT;
  }

  @Override
  public boolean isSelected(@NotNull AnActionEvent e) {
    var editor = findSplitEditor(e);
    if (editor == null) return false;

    return editor.getLayout() == layout;
  }

  @Override
  public void setSelected(@NotNull AnActionEvent e, boolean state) {
    var editor = findSplitEditor(e);
    if (editor == null) return;

    editor.setLayout(layout);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);

    var editor = findSplitEditor(e);
    if (editor == null) return;
    e.getPresentation().setIcon(layout.getIcon(editor));
  }

  static class EditorOnly extends JdlChangePreviewLayoutAction {
    EditorOnly() {
      super(TextEditorWithPreview.Layout.SHOW_EDITOR);
    }
  }

  static class EditorAndPreview extends JdlChangePreviewLayoutAction {
    EditorAndPreview() {
      super(TextEditorWithPreview.Layout.SHOW_EDITOR_AND_PREVIEW);
    }
  }

  static class PreviewOnly extends JdlChangePreviewLayoutAction {
    PreviewOnly() {
      super(TextEditorWithPreview.Layout.SHOW_PREVIEW);
    }
  }
}
