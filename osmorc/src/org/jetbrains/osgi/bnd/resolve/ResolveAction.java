/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.jetbrains.osgi.bnd.resolve;

import aQute.bnd.build.Run;
import aQute.bnd.build.Workspace;
import aQute.bnd.build.model.BndEditModel;
import aQute.bnd.build.model.clauses.VersionedClause;
import aQute.bnd.header.Attrs;
import aQute.bnd.osgi.Constants;
import aQute.bnd.properties.IDocument;
import biz.aQute.resolve.ProjectResolver;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.osgi.bnd.BndFileType;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.service.resolver.ResolutionException;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.*;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static org.osmorc.i18n.OsmorcBundle.message;

public class ResolveAction extends AnAction {
  private static final Logger LOG = Logger.getInstance(ResolveAction.class);

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
             ProjectResolver projectResolver = new ProjectResolver(run)) {
          resolveResult = projectResolver.resolve();

          List<VersionedClause> versionedClauses = projectResolver.getRunBundles().stream()
            .map(c -> {
              Version left = new Version(c.getVersion());
              Version right = new Version(left.getMajor(), left.getMinor(), left.getMicro() + 1);
              VersionRange range = new VersionRange(VersionRange.LEFT_CLOSED, left, right, VersionRange.RIGHT_OPEN);
              Attrs attrs = new Attrs();
              attrs.put(Constants.VERSION_ATTRIBUTE, range.toString());
              return new VersionedClause(c.getBundleSymbolicName(), attrs);
            })
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
            .withName("Bndrun Resolve")
            .run(() -> document.setText(updatedText));
        }
      }

      @Override
      public void onThrowable(@NotNull Throwable t) {
        Throwable cause = t instanceof WrappingException ? t.getCause() : t;
        LOG.warn("Resolution failed", cause);
        if (cause instanceof ResolutionException) {
          new ResolutionFailedDialog(project, (ResolutionException)cause).show();
        }
        else {
          OsmorcBundle.notification(message("bnd.resolve.failed.title"), cause.getMessage(), NotificationType.ERROR).notify(project);
        }
      }
    }.queue();
  }

  @Override
  public void update(@NotNull AnActionEvent event) {
    VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
    event.getPresentation().setEnabledAndVisible(virtualFile != null && BndFileType.BND_RUN_EXT.equals(virtualFile.getExtension()));
  }

  private static class WrappingException extends RuntimeException {
    private WrappingException(Throwable cause) {
      super(cause);
    }
  }

  private static class ResolutionSucceedDialog extends DialogWrapper {
    private final Map<Resource, List<Wire>> myResolveResult;

    public ResolutionSucceedDialog(Project project, Map<Resource, List<Wire>> resolveResult) {
      super(project);
      myResolveResult = resolveResult;
      init();
      setTitle(message("bnd.resolve.succeed.title"));
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
      return new ResolveConfirm(myResolveResult).getContentPane();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
      return new Action[]{getOKAction(), getCancelAction()};
    }
  }

  private static class ResolutionFailedDialog extends DialogWrapper {
    private final ResolutionException myResolutionException;

    public ResolutionFailedDialog(Project project, ResolutionException resolutionException) {
      super(project);
      myResolutionException = resolutionException;
      init();
      setTitle(message("bnd.resolve.failed.title"));
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
      return new ResolutionFailed(myResolutionException).getContentPane();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
      return new Action[]{getOKAction()};
    }
  }
}