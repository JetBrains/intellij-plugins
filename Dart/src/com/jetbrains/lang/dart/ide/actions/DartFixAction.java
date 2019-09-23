// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import org.dartlang.analysis.server.protocol.SourceFileEdit;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * This action is currently not enabled in the Dart Plugin <code>plugin.xml<code/> file.
 * <p/>
 * This is currently experimental work.
 */
public class DartFixAction extends AbstractDartFileProcessingAction {

  public DartFixAction() {
    super(DartBundle.message("dart.fix.action.name"), DartBundle.message("dart.fix.action.description"), null);
  }

  @NotNull
  @Override
  protected String getActionTextForEditor() {
    return DartBundle.message("dart.fix.action.name");
  }

  @NotNull
  @Override
  protected String getActionTextForFiles() {
    return DartBundle.message("dart.fix.action.name.ellipsis"); // because with dialog
  }

  @Override
  protected void runOverEditor(@NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile psiFile) {
    final Document document = editor.getDocument();
    if (!ReadonlyStatusHandler.ensureDocumentWritable(project, document)) return;

    final DartAnalysisServerService das = DartAnalysisServerService.getInstance(project);
    das.updateFilesContent();
    List<SourceFileEdit> sourceFileEdits = das.edit_dartfixNNBD(Collections.singletonList(psiFile.getVirtualFile()));
    if (sourceFileEdits == null) {
      return;
    }

    // TODO(jwren) See DartStyleAction for potential ideas around inline editor notifications

    final Runnable runnable = () -> {
      for (SourceFileEdit edit : sourceFileEdits) {
        AssistUtils.applyFileEdit(project, edit);
      }
    };

    ApplicationManager.getApplication().runWriteAction(
      () -> CommandProcessor.getInstance().executeCommand(project, runnable, DartBundle.message("dart.fix.action.name"), null));
  }

  @Override
  protected void runOverFiles(@NotNull Project project, @NotNull List<VirtualFile> dartFiles) {
    // TODO(jwren)
  }
}
