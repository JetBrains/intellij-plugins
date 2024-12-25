// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.CurrentContentRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.PerforceCachingContentRevision;
import org.jetbrains.idea.perforce.perforce.PerforceContentRevision;
import org.jetbrains.idea.perforce.perforce.ResolvedFile;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ChangeCreator {
  private final Project myProject;
  final Set<VirtualFile> reportedChanges = new HashSet<>();

  public ChangeCreator(final Project project) {
    myProject = project;
  }

  public Change createEditedFileChange(final FilePath path, final long haveRevision, boolean isResolvedWithConflict) {
    return createChange(PerforceCachingContentRevision.create(myProject, path, haveRevision),
                      CurrentContentRevision.create(path),
                      isResolvedWithConflict ? FileStatus.MERGED_WITH_CONFLICTS : FileStatus.MODIFIED);
  }

  public Change createAddedFileChange(final FilePath path, boolean isResolvedWithConflict) {
    return createChange(null, CurrentContentRevision.create(path),
                      isResolvedWithConflict ? FileStatus.MERGED_WITH_CONFLICTS : FileStatus.ADDED);
  }

  public Change createDeletedFileChange(final @NotNull File file, final long haveRevision, boolean isResolvedWithConflict) {
    return createChange(PerforceCachingContentRevision.create(myProject, VcsUtil.getFilePath(file, false), haveRevision), null,
                      isResolvedWithConflict ? FileStatus.MERGED_WITH_CONFLICTS : FileStatus.DELETED);
  }

  public Change createRenameChange(final @NotNull P4Connection connection, final ResolvedFile resolvedFile, final FilePath afterPath) {
    long revision = resolvedFile.getRevision2();
    if (revision < 0) {
      revision = resolvedFile.getRevision1();
    }
    // TODO CACHE IT!!!!
    ContentRevision beforeRevision = PerforceContentRevision.create(myProject, connection, resolvedFile.getDepotPath(), revision, -1);
    ContentRevision afterRevision = CurrentContentRevision.create(afterPath);
    return createChange(beforeRevision, afterRevision, FileStatus.MODIFIED);
  }

  private Change createChange(final @Nullable ContentRevision before, final @Nullable ContentRevision after, final FileStatus fileStatus) {
    final Change change = new Change(before, after, fileStatus);
    if (after != null) {
      ContainerUtil.addIfNotNull(reportedChanges, after.getFile().getVirtualFile());
    }
    if (before != null) {
      ContainerUtil.addIfNotNull(reportedChanges, before.getFile().getVirtualFile());
    }
    return change;
  }
}
