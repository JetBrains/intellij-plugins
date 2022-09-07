// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.bnd.resolve;

import aQute.bnd.build.Run;
import aQute.bnd.build.Workspace;
import aQute.bnd.build.model.BndEditModel;
import aQute.bnd.build.model.clauses.VersionedClause;
import aQute.bnd.osgi.resource.ResourceUtils;
import aQute.bnd.properties.IDocument;
import biz.aQute.resolve.ProjectResolver;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.bnd.BndFileType;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.service.resolver.ResolutionException;
import org.osmorc.i18n.OsmorcBundle;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static org.osmorc.i18n.OsmorcBundle.message;

public class ResolveAction extends DumbAwareAction {
  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void update(@NotNull AnActionEvent event) {
    VirtualFile file;
    event.getPresentation().setEnabledAndVisible(
      event.getProject() != null &&
      (file = event.getData(CommonDataKeys.VIRTUAL_FILE)) != null &&
      BndFileType.BND_RUN_EXT.equals(file.getExtension()));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
    Project project = event.getProject();
    if (virtualFile == null || project == null) return;
    Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
    if (document == null) return;

    FileDocumentManager.getInstance().saveAllDocuments();

    new Task.Backgroundable(project, message("bnd.resolve.requirements.title"), true) {
      private Map<Resource, List<Wire>> resolveResult;
      private String updatedText;

      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        indicator.setIndeterminate(true);

        File file = new File(virtualFile.getPath());
        try (Workspace workspace = Workspace.findWorkspace(file);
             Run run = Run.createRun(workspace, file);
             @SuppressWarnings("deprecation") ProjectResolver projectResolver = new ProjectResolver(run)) {
          resolveResult = projectResolver.resolve();

          List<VersionedClause> versionedClauses = resolveResult.keySet().stream()
            .map(resource -> ResourceUtils.toVersionClause(resource, "[===,==+)"))
            .sorted(Comparator.comparing(VersionedClause::getName))
            .collect(Collectors.toList());

          BndEditModel editModel = new BndEditModel();
          IDocument bndDocument = new aQute.bnd.properties.Document(document.getImmutableCharSequence().toString());
          editModel.loadFrom(bndDocument);
          editModel.setRunBundles(versionedClauses);
          editModel.saveChangesTo(bndDocument);
          updatedText = bndDocument.get();
        }
        catch (ProcessCanceledException e) { throw e; }
        catch (Exception e) { throw new WrappingException(e); }

        indicator.checkCanceled();
      }

      @Override
      public void onSuccess() {
        if (new ResolutionSucceedDialog(project, resolveResult).showAndGet() &&
            FileModificationService.getInstance().prepareVirtualFilesForWrite(project, Collections.singleton(virtualFile))) {
          writeCommandAction(project)
            .withName(message("bnd.resolve.command"))
            .run(() -> document.setText(updatedText));
        }
      }

      @Override
      public void onThrowable(@NotNull Throwable t) {
        Throwable cause = t instanceof WrappingException ? t.getCause() : t;
        Logger.getInstance(ResolveAction.class).warn("resolution failed", cause);
        if (cause instanceof ResolutionException) {
          new ResolutionFailedDialog(project, (ResolutionException)cause).show();
        }
        else {
          OsmorcBundle.notification(message("bnd.resolve.failed.notification"), cause.getMessage(), NotificationType.ERROR).notify(project);
        }
      }
    }.queue();
  }

  private static final class WrappingException extends RuntimeException {
    private WrappingException(Throwable cause) {
      super(cause);
    }
  }
}
