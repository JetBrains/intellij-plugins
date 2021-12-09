/*
 * Copyright 2000-2007 JetBrains s.r.o.
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
import com.intellij.openapi.vcs.changes.BinaryContentRevision;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceContentRevision;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;


public class PerforceBinaryContentRevision extends PerforceContentRevision implements BinaryContentRevision {
  private byte[] myContent = null;

  public PerforceBinaryContentRevision(final Project project, final FilePath path, final long revision) {
    super(project, path, revision);
  }

  public PerforceBinaryContentRevision(@NotNull Project project,
                                       @Nullable P4Connection connection,
                                       @Nullable String depotPath,
                                       @Nullable FilePath filePath, long revision, @NotNull String stringRevision) {
    super(project, connection, depotPath, filePath, revision, stringRevision);
  }

  @Override
  public byte @Nullable [] getContentAsBytes() throws VcsException {
    if (myContent != null) return myContent;

    final P4File p4File = P4File.create(myFilePath);
    final PerforceSettings settings = PerforceSettings.getSettings(myProject);
    if (!settings.ENABLED) return null;
    String rev = myStringRevision.startsWith("@=") ? myStringRevision :
                 "#" + (myRevision > 0 ? myRevision : p4File.getFstat(myProject, false).haveRev);
    final byte[] bytes = PerforceRunner.getInstance(myProject).getByteContent(p4File, rev);
    if (bytes != null) {
      myContent = bytes;
    }

    return myContent;
  }

  @Override
  public byte @Nullable [] getBinaryContent() throws VcsException {
    return getContentAsBytes();
  }
}
