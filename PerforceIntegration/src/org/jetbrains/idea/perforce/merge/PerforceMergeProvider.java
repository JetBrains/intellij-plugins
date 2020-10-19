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
package org.jetbrains.idea.perforce.merge;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.TextRevisionNumber;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.merge.MergeData;
import com.intellij.openapi.vcs.merge.MergeDialogCustomizer;
import com.intellij.openapi.vcs.merge.MergeProvider;
import com.intellij.openapi.vcs.merge.MultipleFileMergeDialog;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.vcsUtil.VcsRunnable;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.FileGrouper;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PerforceMergeProvider implements MergeProvider {
  private final Project myProject;
  private final PerforceRunner myRunner;

  public PerforceMergeProvider(final Project project) {
    myProject = project;
    myRunner = PerforceRunner.getInstance(myProject);
  }

  @Override
  @NotNull
  public MergeData loadRevisions(@NotNull final VirtualFile file) throws VcsException {
    final MergeData mergeData = new MergeData();
    VcsRunnable runnable = new VcsRunnable() {
      @Override
      public void run() throws VcsException {
        final PerforceSettings settings = PerforceSettings.getSettings(myProject);
        final P4File p4File = P4File.create(file);
        BaseRevision baseRevision = myRunner.getBaseRevision(p4File);
        if (baseRevision == null) {
          throw new VcsException(PerforceBundle.message("message.text.cannot.find.merge.info.for.file", file.getPresentableUrl()));
        }

        final P4Connection connection = settings.getConnectionForFile(file);
        mergeData.ORIGINAL = myRunner.getByteContent(baseRevision, connection);
        String sourceRevision = baseRevision.getSourceRevision();
        if (sourceRevision == null) {
          final FStat fStat = p4File.getFstat(myProject, true);
          mergeData.LAST_REVISION_NUMBER = new VcsRevisionNumber.Int(Integer.parseInt(fStat.haveRev));

          mergeData.LAST = myRunner.getByteContent(file.getPath(), null, connection);
        }
        else {
          mergeData.LAST = myRunner.getByteContent(baseRevision.getDepotPath(), sourceRevision, connection);
          mergeData.LAST_REVISION_NUMBER = new TextRevisionNumber(sourceRevision);
        }

        try {
          mergeData.CURRENT = file.contentsToByteArray();
        }
        catch (IOException e) {
          throw new VcsException(e);
        }
      }
    };
    VcsUtil.runVcsProcessWithProgress(runnable, VcsBundle.message("multiple.file.merge.loading.progress.title"), false, myProject);
    return mergeData;

  }

  @Override
  public void conflictResolvedForFile(@NotNull final VirtualFile file) {
    try {
      VcsUtil.runVcsProcessWithProgress(() -> myRunner.resolveToYours(P4File.create(file)),
                                        PerforceBundle.message("progress.marking.file.as.resolved"), false, myProject);
    }
    catch (VcsException ignore) {
    }
  }

  @Override
  public boolean isBinary(@NotNull final VirtualFile file) {
    return false;
  }

  public List<VirtualFile> showMergeDialog(List<VirtualFile> files) {
    if (files.isEmpty()) return Collections.emptyList();
    
    final MultipleFileMergeDialog fileMergeDialog = new PerforceMergeDialog(files);
    fileMergeDialog.show();
    return fileMergeDialog.getProcessedFiles();
  }

  private class PerforceMergeDialog extends MultipleFileMergeDialog {
    PerforceMergeDialog(List<VirtualFile> files) {
      super(myProject, files, PerforceMergeProvider.this, new MergeDialogCustomizer());
    }

    @Override
    protected boolean beforeResolve(Collection<VirtualFile> files) {
      for (Map.Entry<P4Connection, Collection<VirtualFile>> entry : FileGrouper.distributeFilesByConnection(files, myProject).entrySet()) {
        if (!openForEdit(entry.getKey(), entry.getValue())) {
          return false;
        }
      }
      return true;
    }

    private boolean openForEdit(final P4Connection connection, final Collection<VirtualFile> files) {
      List<P4File> p4Files = ContainerUtil.map(files, P4File::create);

      try {
        myRunner.editAll(p4Files, -1, false, connection);
      }
      catch (VcsException e) {
        AbstractVcsHelper.getInstance(myProject).showError(e, PerforceBundle.message("message.title.resolve"));
        return false;
      }
      VfsUtil.markDirtyAndRefresh(false, false, false, files.toArray(VirtualFile.EMPTY_ARRAY));
      
      return true;
    }
  }
}
