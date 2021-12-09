// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
  @NotNull
  @Override
  protected String getActionTextForEditor() {
    return DartBundle.message("action.Dart.DartFix.text");
  }

  @NotNull
  @Override
  protected String getActionTextForFiles() {
    return DartBundle.message("dart.fix.action.name.ellipsis"); // because with dialog
  }

  @Override
  protected void runOverEditor(@NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile psiFile) {
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
