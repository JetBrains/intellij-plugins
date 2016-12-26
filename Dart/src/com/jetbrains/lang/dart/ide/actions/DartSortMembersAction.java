/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.ide.actions;

import com.google.common.collect.Maps;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import org.dartlang.analysis.server.protocol.SourceEdit;
import org.dartlang.analysis.server.protocol.SourceFileEdit;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DartSortMembersAction extends AbstractDartFileProcessingAction {

  private static final Logger LOG = Logger.getInstance(DartSortMembersAction.class.getName());

  public DartSortMembersAction() {
    super(DartBundle.message("dart.sort.members.action.name"), DartBundle.message("dart.sort.members.action.description"), null);
  }

  @NotNull
  @Override
  protected String getActionTextForEditor() {
    return DartBundle.message("dart.sort.members.action.name");
  }

  @NotNull
  @Override
  protected String getActionTextForFiles() {
    return DartBundle.message("dart.sort.members.action.name.ellipsis"); // because with dialog
  }

  protected void runOverEditor(@NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile psiFile) {
    final Document document = editor.getDocument();
    if (!ReadonlyStatusHandler.ensureDocumentWritable(project, document)) return;

    final String path = psiFile.getVirtualFile().getPath();

    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(project);
    service.updateFilesContent();
    final SourceFileEdit fileEdit = service.edit_sortMembers(path);

    if (fileEdit == null) {
      showHintLater(editor, DartBundle.message("dart.sort.members.hint.failed"), true);
      LOG.warn("Unexpected response from edit_sortMembers, fileEdit is null");
      return;
    }

    final List<SourceEdit> edits = fileEdit.getEdits();
    if (edits == null || edits.size() == 0) {
      showHintLater(editor, DartBundle.message("dart.sort.members.hint.already.good"), false);
      return;
    }

    final Runnable runnable = () -> {
      AssistUtils.applySourceEdits(project, psiFile.getVirtualFile(), document, edits, Collections.emptySet());
      showHintLater(editor, DartBundle.message("dart.sort.members.hint.success"), false);
    };

    ApplicationManager.getApplication().runWriteAction(
      () -> CommandProcessor.getInstance().executeCommand(project, runnable, DartBundle.message("dart.sort.members.action.name"), null));
  }

  protected void runOverFiles(@NotNull final Project project, @NotNull final List<VirtualFile> dartFiles) {
    if (dartFiles.isEmpty()) {
      Messages.showInfoMessage(project, DartBundle.message("dart.sort.members.files.no.dart.files"),
                               DartBundle.message("dart.sort.members.action.name"));
      return;
    }

    if (Messages.showOkCancelDialog(project, DartBundle.message("dart.sort.members.files.dialog.question", dartFiles.size()),
                                    DartBundle.message("dart.sort.members.action.name"), null) != Messages.OK) {
      return;
    }

    final Map<VirtualFile, SourceFileEdit> fileToFileEditMap = Maps.newHashMap();

    final Runnable runnable = () -> {
      double fraction = 0.0;
      for (final VirtualFile virtualFile : dartFiles) {
        fraction += 1.0;
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator != null) {
          indicator.checkCanceled();
          indicator.setFraction(fraction / dartFiles.size());
          indicator.setText2(FileUtil.toSystemDependentName(virtualFile.getPath()));
        }

        final String path = virtualFile.getPath();
        final SourceFileEdit fileEdit = DartAnalysisServerService.getInstance(project).edit_sortMembers(path);
        if (fileEdit != null) {
          fileToFileEditMap.put(virtualFile, fileEdit);
        }
      }
    };

    DartAnalysisServerService.getInstance(project).updateFilesContent();

    final boolean ok = ApplicationManagerEx.getApplicationEx()
      .runProcessWithProgressSynchronously(runnable, DartBundle.message("dart.sort.members.action.name"), true, project);

    if (ok) {
      final Runnable onSuccessRunnable = () -> {
        CommandProcessor.getInstance().markCurrentCommandAsGlobal(project);

        for (Map.Entry<VirtualFile, SourceFileEdit> entry : fileToFileEditMap.entrySet()) {
          final VirtualFile file = entry.getKey();
          final Document document = FileDocumentManager.getInstance().getDocument(file);
          final SourceFileEdit fileEdit = entry.getValue();
          if (document != null) {
            AssistUtils.applySourceEdits(project, file, document, fileEdit.getEdits(), Collections.emptySet());
          }
        }
      };

      ApplicationManager.getApplication().runWriteAction(() -> CommandProcessor.getInstance()
        .executeCommand(project, onSuccessRunnable, DartBundle.message("dart.sort.members.action.name"), null));
    }
  }
}
