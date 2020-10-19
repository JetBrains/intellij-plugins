/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package org.jetbrains.idea.perforce.actions;

import com.intellij.CommonBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.CancelActionException;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.P4File;

import java.util.ArrayList;
import java.util.List;

public abstract class ActionBaseFile extends DumbAwareAction {

  private static final Logger LOG = Logger.getInstance(ActionBaseFile.class);

  protected final static String[] YES_NO_OPTIONS = {CommonBundle.getYesButtonText(), CommonBundle.getNoButtonText()};
  protected final static String[] YES_NO_CANCELREST_OPTIONS = {CommonBundle.getYesButtonText(), CommonBundle.getNoButtonText(),
    PerforceBundle.message("button.text.cancel.rest")};

  protected static void log(@NonNls final String msg) {
    LOG.debug(msg);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    Presentation presentation = e.getPresentation();

    Project project = e.getData(CommonDataKeys.PROJECT);
    if (project == null) {
      presentation.setEnabled(false);
      return;
    }

    VirtualFile[] vFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
    if (ArrayUtil.isEmpty(vFiles) ||
        !ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(PerforceVcs.getInstance(project), vFiles)) {
      presentation.setEnabled(false);
      return;
    }

    presentation.setEnabled(true);
  }

  protected abstract void performAction(final VirtualFile vFile,
                                        Project project,
                                        final boolean alone,
                                        final List<VirtualFile> filesToPostProcess) throws CancelActionException, VcsException;

  @Override
  public void actionPerformed(@NotNull final AnActionEvent event) {
    Project project = event.getRequiredData(CommonDataKeys.PROJECT);
    VirtualFile[] vFiles = event.getRequiredData(CommonDataKeys.VIRTUAL_FILE_ARRAY);

    FileDocumentManager.getInstance().saveAllDocuments();

    processFiles(project, vFiles);
  }

  public void processFiles(final Project project, final VirtualFile... vFiles) {
    boolean containsDirectory = false;
    if (vFiles != null && vFiles.length > 0) {
      try {
        List<VirtualFile> filesToPostProcess = new ArrayList<>();
        for (final VirtualFile vFile : vFiles) {
          final boolean[] cancelled = new boolean[1];
          cancelled[0] = false;
          try {
            try {
              performAction(vFile, project, (vFiles.length == 1), filesToPostProcess);
              if (vFile.isDirectory()) {
                containsDirectory = true;
              }
            }
            catch (VcsException e) {
              AbstractVcsHelper.getInstance(project).showError(e, PerforceBundle.message("dialog.title.perforce"));
            }
          }
          catch (CancelActionException e) {
            cancelled[0] = true;
          }
          if (cancelled[0]) {
            break;
          }
        }
        postProcessFiles(project, filesToPostProcess);
      }
      finally {
        if (containsDirectory) {
          VirtualFileManager.getInstance().asyncRefresh(() -> {
            P4File.invalidateFstat(project);
            for (VirtualFile file : vFiles) {
              if (file.isDirectory()) {
                VcsDirtyScopeManager.getInstance(project).dirDirtyRecursively(file);
              }
              else {
                VcsDirtyScopeManager.getInstance(project).fileDirty(file);
              }
            }
          });
        }
        else {
          for (final VirtualFile vFile : vFiles) {
            ApplicationManager.getApplication().runWriteAction(() -> vFile.refresh(false, false));
            P4File.invalidateFstat(vFile);
            VcsDirtyScopeManager.getInstance(project).fileDirty(vFile);
          }
        }
      }
    }
  }

  public void postProcessFiles(final Project project, final List<VirtualFile> filesToPostProcess) {
  }

}
