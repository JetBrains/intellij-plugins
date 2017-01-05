/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
import aQute.bnd.properties.Document;
import biz.aQute.resolve.ProjectResolver;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.service.resolver.ResolutionException;

import javax.swing.*;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResolveAction extends AnAction {

  private static final Logger LOG = Logger.getInstance(ResolveAction.class);

  @Override
  public void actionPerformed(AnActionEvent event) {
    VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);

    if (virtualFile == null) {
      // This should never happen as the action is only available in when the virtualFile name ends with ".bndrun"
      return;
    }

    try {
      File file = new File(virtualFile.getCanonicalPath());
      Workspace workspace = Workspace.findWorkspace(file);
      Run run = Run.createRun(workspace, file);

      try (ProjectResolver projectResolver = new ProjectResolver(run)) {
        Map<Resource, List<Wire>> resolveResult = projectResolver.resolve();

        DialogBuilder dialogBuilder = new DialogBuilder();
        dialogBuilder.setTitle("Confirm resolution");
        dialogBuilder.setCenterPanel(new ResolveConfirm(resolveResult).contentPane);

        dialogBuilder.setOkOperation(() -> ApplicationManager.getApplication().runWriteAction(() -> {
          try {
            List<VersionedClause> versionedClauses = projectResolver.getRunBundles().stream()
              .map(c -> {
                Attrs attrs = new Attrs();
                attrs.put(Constants.VERSION_ATTRIBUTE, c.getVersion());
                return new VersionedClause(c.getBundleSymbolicName(), attrs);
              })
              .sorted(Comparator.comparing(VersionedClause::getName))
              .collect(Collectors.toList());

            BndEditModel editModel = new BndEditModel();
            Document document = new Document(VfsUtilCore.loadText(virtualFile));
            editModel.loadFrom(document);
            editModel.setRunBundles(versionedClauses);
            editModel.saveChangesTo(document);
            VfsUtil.saveText(virtualFile, document.get());
          }
          catch (Exception e) {
            Notifications.Bus.notify(new Notification("Osmorc",
                                                      "Failed to save resolution results",
                                                      e.getMessage(),
                                                      NotificationType.ERROR));
            LOG.warn("Failed to save resolution results", e);
          }
          finally {
            dialogBuilder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
          }
        }));
        dialogBuilder.showModal(true);
      }
    }
    catch (ResolutionException resolutionException) {
      DialogWrapper wrapper = new ResolutionFailedDialogWrapper(resolutionException);
      wrapper.show();
    }
    catch (Exception e) {
      Notifications.Bus.notify(new Notification("Osmorc",
                                                "Resolution failed",
                                                e.getMessage(),
                                                NotificationType.ERROR));
      LOG.warn("Resolution failed", e);
    }
  }

  @Override
  public void update(AnActionEvent actionEvent) {
    VirtualFile virtualFile = actionEvent.getData(CommonDataKeys.VIRTUAL_FILE);
    if (virtualFile != null && "bndrun".equals(virtualFile.getExtension())) {
      actionEvent.getPresentation().setVisible(true);
    } else {
      actionEvent.getPresentation().setVisible(false);
    }
  }

  private static class ResolutionFailedDialogWrapper extends DialogWrapper {

    private final ResolutionException myResolutionException;

    public ResolutionFailedDialogWrapper(ResolutionException resolutionException) {
      super((Project)null, false);
      myResolutionException = resolutionException;
      init();
      setTitle("Resolution Failed");
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
