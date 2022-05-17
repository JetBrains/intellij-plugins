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
package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.vfs.VcsFileSystem;
import com.intellij.openapi.vcs.vfs.VcsVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.vcs.AnnotationProviderEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.annotation.AnnotationInfo;
import org.jetbrains.idea.perforce.application.annotation.PerforceFileAnnotation;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.P4Revision;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

public class PerforceAnnotationProvider implements AnnotationProviderEx {
  private final Project myProject;
  private final PerforceRunner myRunner;

  public PerforceAnnotationProvider(final Project project) {
    myProject = project;
    myRunner = PerforceRunner.getInstance(project);
  }

  @NotNull
  @Override
  public FileAnnotation annotate(@NotNull VirtualFile file) throws VcsException {
    FilePath filePath = VcsContextFactory.getInstance().createFilePathOn(file);
    return doAnnotate(file, ChangesUtil.getCommittedPath(myProject, filePath), -1);
  }

  @NotNull
  private FileAnnotation doAnnotate(final VirtualFile vFile, final FilePath file, final long changeNumber) throws VcsException {
    P4Connection connection = PerforceSettings.getSettings(myProject).getConnectionForFile(vFile);
    if (connection == null) {
      throw new VcsException(PerforceBundle.message("error.invalid.perforce.settings"));
    }

    return doAnnotate(changeNumber, connection, P4File.create(file).getEscapedPath()).createAnnotation(vFile);
  }

  @NotNull
  private AnnotationPrecursor doAnnotate(final long changeNumber, P4Connection connection, @NotNull final String path) throws VcsException {
    final P4Revision[] fileLog = myRunner.filelog(connection, path, true);
    P4Revision p4Revision = ContainerUtil.find(fileLog, p4Revision1 -> p4Revision1.getChangeNumber() == changeNumber);
    String pathAtRevision = p4Revision == null ? path : p4Revision.getDepotPath();
    long revision = p4Revision == null ? -1 : p4Revision.getRevisionNumber();

    final AnnotationInfo annotationInfo = myRunner.annotate(connection, pathAtRevision, revision);
    return new AnnotationPrecursor(annotationInfo, fileLog, revision, p4Revision, connection);
  }

  private class AnnotationPrecursor {
    final AnnotationInfo info;
    final P4Revision[] fileLog;
    final long changeNumber;
    @Nullable final P4Revision p4Revision;
    final P4Connection connection;

    AnnotationPrecursor(AnnotationInfo info, P4Revision[] fileLog, long changeNumber, @Nullable P4Revision p4Revision, P4Connection connection) {
      this.info = info;
      this.fileLog = fileLog;
      this.changeNumber = changeNumber;
      this.p4Revision = p4Revision;
      this.connection = connection;
    }
    
    FileAnnotation createAnnotation(@NotNull VirtualFile file) {
      return new PerforceFileAnnotation(info, file, fileLog, myProject, connection, changeNumber);
    }
  }

  @NotNull
  @Override
  public FileAnnotation annotate(@NotNull VirtualFile file, VcsFileRevision revision) throws VcsException {
    PerforceVcsRevisionNumber number = (PerforceVcsRevisionNumber) revision.getRevisionNumber();
    FilePath filePath = VcsContextFactory.getInstance().createFilePathOn(file);
    return doAnnotate(file, filePath, number.getChangeNumber());
  }

  @NotNull
  @Override
  public FileAnnotation annotate(@NotNull FilePath path, @NotNull VcsRevisionNumber number) throws VcsException {
    PerforceSettings settings = PerforceSettings.getSettings(myProject);
    P4Connection connection = settings.getConnectionForFile(path.getIOFile());
    if (connection == null) {
      throw new VcsException(PerforceBundle.message("error.cannot.find.perforce.root", path));
    }

    long changeNumber = ((PerforceVcsRevisionNumber)number).getChangeNumber();
    AnnotationPrecursor precursor = doAnnotate(changeNumber, connection, ((PerforceVcsRevisionNumber)number).getDepotPath());
    if (precursor.p4Revision == null) {
      throw new VcsException(PerforceBundle.message("error.cannot.find.perforce.revision", changeNumber, path));
    }

    PerforceFileRevision revision = new PerforceFileRevision(precursor.p4Revision, connection, myProject);
    return precursor.createAnnotation(new VcsVirtualFile(path.getPath(), revision, VcsFileSystem.getInstance()));
  }
}
