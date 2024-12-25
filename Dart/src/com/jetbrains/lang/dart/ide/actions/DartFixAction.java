// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This action is currently not enabled in the Dart Plugin <code>plugin.xml<code/> file.
 * <p/>
 * This is currently experimental work.
 */
@SuppressWarnings("ComponentNotRegistered")
public class DartFixAction extends AbstractDartFileProcessingAction {
  @Override
  protected @NotNull String getActionTextForEditor() {
    return DartBundle.message("action.Dart.DartFix.text");
  }

  @Override
  protected @NotNull String getActionTextForFiles() {
    return DartBundle.message("dart.fix.action.name.ellipsis"); // because with dialog
  }

  @Override
  protected void runOverEditor(final @NotNull Project project, final @NotNull Editor editor, final @NotNull PsiFile psiFile) {
    //final Document document = editor.getDocument();
    //if (!ReadonlyStatusHandler.ensureDocumentWritable(project, document)) return;

    // TODO(jwren) edit_dartfix has been removed in lieu of `dart fix *`, this code needs to be updated before this action is exposed.

    // TODO(jwren) See DartStyleAction for potential ideas around inline editor notifications
  }

  @Override
  protected void runOverFiles(@NotNull Project project, @NotNull List<VirtualFile> dartFiles) {
    // TODO(jwren)
  }
}
