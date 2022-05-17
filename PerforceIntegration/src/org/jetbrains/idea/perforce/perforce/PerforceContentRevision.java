/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.ByteBackedContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.impl.ContentRevisionCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.*;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.io.File;
import java.io.IOException;

public class PerforceContentRevision implements ByteBackedContentRevision {
  protected final Project myProject;
  @Nullable private final P4Connection myConnection;
  @Nullable private final String myDepotPath;
  protected final long myRevision;
  protected final String myStringRevision;
  protected FilePath myFilePath;

  public PerforceContentRevision(final Project project, final FilePath path, final long revision) {
    this(project, null, null, path, revision, "#" + revision);
  }

  protected PerforceContentRevision(@NotNull Project project,
                                    @Nullable P4Connection connection,
                                    @Nullable String depotPath,
                                    @Nullable FilePath filePath,
                                    long revision,
                                    @NotNull String stringRevision) {
    myFilePath = filePath;
    myStringRevision = stringRevision;
    myRevision = revision;
    myDepotPath = depotPath;
    myConnection = connection;
    myProject = project;
  }

  @Override
  @Nullable
  public String getContent() throws VcsException {
    String content = ContentRevisionCache.getAsString(getContentAsBytes(), getFile(), null);
    // todo whether we need to convert them?
    return content == null ? null : StringUtil.convertLineSeparators(content, System.lineSeparator());
  }

  @Override
  public byte @Nullable [] getContentAsBytes() throws VcsException {
    try {
      return ContentRevisionCache.getOrLoadAsBytes(myProject, getFile(), getRevisionNumber(), PerforceVcs.getKey(),
                                                   ContentRevisionCache.UniqueType.REPOSITORY_CONTENT,
                                                   () -> loadContent());
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  protected byte @NotNull [] loadContent() throws VcsException {
    PerforceRunner runner = PerforceRunner.getInstance(myProject);
    if (myDepotPath != null && myConnection != null) {
      return runner.getByteContent(myDepotPath, myStringRevision, myConnection);
    }
    return runner.getContent(getFile(), myStringRevision);
  }

  @Override
  @NotNull
  public FilePath getFile() {
    if (myFilePath == null) {
      if ((! myProject.isDisposed()) && PerforceSettings.getSettings(myProject).ENABLED) {
        PerforceClient client = PerforceManager.getInstance(myProject).getClient(myConnection);
        File file;
        try {
          file = PerforceManager.getFileByDepotName(myDepotPath, client);
        }
        catch (VcsException e) {
          file = null;
        }
        if (file != null) {
          myFilePath = VcsContextFactory.getInstance().createFilePathOn(file);
        }
      }
      if (myFilePath == null) {
        myFilePath = VcsContextFactory.getInstance().createFilePathOnNonLocal(myDepotPath, false);
      }
    }
    return myFilePath;
  }

  @Override
  @NotNull
  public VcsRevisionNumber getRevisionNumber() {
    return new PerforceOnlyRevisionNumber(myRevision);
  }

  public String getDepotPath() {
    return myDepotPath;
  }

  public long getRevision() {
    return myRevision;
  }

  public static PerforceContentRevision create(final Project project, String depotPath, final FilePath localPath, final long revision) {
    final FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(localPath.getName());
    if (fileType.isBinary()) {
      return new PerforceBinaryContentRevision(project, null, depotPath, localPath, revision, "#" + revision);
    }

    return new PerforceContentRevision(project, null, depotPath, localPath, revision, "#" + revision);
  }

  public static PerforceContentRevision create(final Project project, @Nullable P4Connection connection, @NotNull String depotPath, final long revision, long shelveChangeList) {
    String stringRevision = shelveChangeList > 0 ? "@=" + shelveChangeList : "#" + revision;

    int fileNamePos = depotPath.lastIndexOf('/');
    if (fileNamePos >= 0) {
      String fileName = depotPath.substring(fileNamePos);
      final FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(fileName);
      if (fileType.isBinary()) {
        return new PerforceBinaryContentRevision(project, connection, depotPath, null, revision, stringRevision);
      }
    }

    return new PerforceContentRevision(project, connection, depotPath, null, revision, stringRevision);
  }

  @Override
  public String toString() {
    return myFilePath.getPath();
  }
}
