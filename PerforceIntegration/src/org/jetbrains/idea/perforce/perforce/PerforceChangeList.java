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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.io.IOUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.ChangeListData;
import org.jetbrains.idea.perforce.application.PerforceClient;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class PerforceChangeList implements CommittedChangeList {
  private static final Logger LOG = Logger.getInstance(PerforceChangeList.class);

  private Date myDate;
  private long myNumber;
  private String myDescription;
  private String myUser;
  private String myClient;
  private final Project myProject;
  private final P4Connection myConnection;
  private List<Change> myIdeaChanges;
  private final PerforceChangeCache myChangeCache;

  public PerforceChangeList(@NotNull final ChangeListData data, final Project project, @NotNull final P4Connection connection, PerforceChangeCache changeCache) {
    myChangeCache = changeCache;
    myUser = data.USER;
    try {
      // Perforce before 2003.1 did not include date in 'p4 changes' output
      if (data.DATE.indexOf(':') >= 0) {
        myDate = ChangeListData.DATE_FORMAT.parse(data.DATE);
      }
      else {
        myDate = ChangeListData.DATE_ONLY_FORMAT.parse(data.DATE);
      }
    }
    catch (ParseException e) {
      LOG.error(e);
      myDate = new Date();
    }
    myNumber = data.NUMBER;
    myDescription = data.DESCRIPTION;
    myClient = data.CLIENT;

    myProject = project;
    myConnection = connection;
  }

  public PerforceChangeList(final Project project,
                            @NotNull DataInput stream,
                            @NotNull final P4Connection connection,
                            final PerforceClient perforceClient,
                            PerforceChangeCache changeCache) throws IOException {
    myProject = project;
    myChangeCache = changeCache;
    myConnection = connection;
    readFromStream(stream, perforceClient);
  }

  @Override
  public String getCommitterName() {
    return myUser;
  }

  @Override
  public Date getCommitDate() {
    return myDate;
  }

  @Override
  public Collection<Change> getChanges() {
    if (myIdeaChanges == null) {
      myIdeaChanges = getChangesUnder(null);
    }
    return myIdeaChanges;
  }

  public List<Change> getChangesUnder(@Nullable final VirtualFile root) {
    List<Change> ideaChanges = new ArrayList<>();
    for(PerforceChange path: myChangeCache.getChanges(myConnection, myNumber, root)) {
      final int type = path.getType();
      PerforceContentRevision beforeRevision = ((type == PerforceAbstractChange.ADD) || (type == PerforceAbstractChange.MOVE_ADD))
                                               ? null
                                               : createRevision(path.getDepotPath(), path.getRevision() - 1);

      PerforceContentRevision afterRevision = ((type == PerforceAbstractChange.DELETE) || (type == PerforceAbstractChange.MOVE_DELETE))
                                              ? null
                                              : createRevision(path.getDepotPath(), path.getRevision());

      ideaChanges.add(new Change(beforeRevision, afterRevision));
    }
    return ideaChanges;
  }

  private PerforceContentRevision createRevision(final String depotPath, final long revision) {
    return PerforceContentRevision.create(myProject, myConnection, depotPath, revision, -1);
  }

  @Override
  @NotNull
  public String getName() {
    return myDescription;
  }

  @Override
  public String getComment() {
    return myDescription;
  }

  @Override
  public long getNumber() {
    return myNumber;
  }

  @Nullable
  @Override
  public String getBranch() {
    return null;
  }

  @Override
  public AbstractVcs getVcs() {
    return PerforceVcs.getInstance(myProject);
  }

  @Override
  public boolean isModifiable() {
    return true;
  }

  @Override
  public void setDescription(String newMessage) {
    myDescription = newMessage;
  }

  @NlsSafe
  public String getClient() {
    return myClient;
  }

  public String toString() {
    return myDescription;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final PerforceChangeList that = (PerforceChangeList)o;

    if (myNumber != that.myNumber) return false;
    if (myClient != null ? !myClient.equals(that.myClient) : that.myClient != null) return false;
    if (myDate != null ? !myDate.equals(that.myDate) : that.myDate != null) return false;
    if (myDescription != null ? !myDescription.equals(that.myDescription) : that.myDescription != null) return false;
    if (myUser != null ? !myUser.equals(that.myUser) : that.myUser != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (myDate != null ? myDate.hashCode() : 0);
    result = 31 * result + (int)(myNumber ^ (myNumber >>> 32));
    result = 31 * result + (myDescription != null ? myDescription.hashCode() : 0);
    result = 31 * result + (myUser != null ? myUser.hashCode() : 0);
    result = 31 * result + (myClient != null ? myClient.hashCode() : 0);
    return result;
  }

  public void writeToStream(@NotNull DataOutput stream) throws IOException {
    stream.writeLong(myNumber);
    stream.writeLong(myDate.getTime());
    stream.writeUTF(myUser);
    stream.writeUTF(myClient);
    IOUtil.writeUTFTruncated(stream, myDescription);
    myConnection.getId().writeToStream(stream);
    Collection<Change> changes = getChanges();
    stream.writeInt(changes.size());
    for (Change change : changes) {
      PerforceContentRevision revision = (PerforceContentRevision)change.getAfterRevision();
      if (revision == null) {
        stream.writeByte(0);
        revision = (PerforceContentRevision) change.getBeforeRevision();
        assert revision != null;
      }
      else {
        stream.writeByte(change.getBeforeRevision() != null ? 1 : 2);
      }
      stream.writeLong(revision.getRevision());
      stream.writeUTF(revision.getDepotPath());
    }
  }

  private void readFromStream(@NotNull DataInput stream, final PerforceClient perforceClient) throws IOException {
    myNumber = stream.readLong();
    myDate = new Date(stream.readLong());
    myUser = stream.readUTF();
    myClient = stream.readUTF();
    myDescription = stream.readUTF();
    // to don't break the serialization contract we keep it here
    ConnectionId.readFromStream(stream);
    int count = stream.readInt();
    myIdeaChanges = new ArrayList<>(count);

    final VcsContextFactory pathService = VcsContextFactory.getInstance();
    for(int i=0; i<count; i++) {
      byte type = stream.readByte();
      long revision = stream.readLong();
      String depotPath = stream.readUTF();
      Change change = null;
      FilePath filePath = createFilePath(depotPath, perforceClient, pathService);
      switch(type) {
        case 0:
          change = new Change(PerforceContentRevision.create(myProject, depotPath, filePath, revision), null);
          break;
        case 1:
          change = new Change(
            PerforceContentRevision.create(myProject, depotPath, filePath, revision - 1),
            PerforceContentRevision.create(myProject, depotPath, filePath, revision));
          break;
        case 2:
          change = new Change(null,
                              PerforceContentRevision.create(myProject, depotPath, filePath, revision));
          break;
        default:
          assert false: "Unknown p4 change type " + type;
      }
      myIdeaChanges.add(change);
    }
  }

  private static FilePath createFilePath(String depotPath, PerforceClient client, VcsContextFactory pathService) {
    try {
      File file = PerforceManager.getFileByDepotName(depotPath, client);
      if (file != null) {
        return pathService.createFilePathOn(file, false);
      }
    }
    catch (VcsException ignore) {
    }
    return pathService.createFilePathOnNonLocal(depotPath, false);
  }

  // The method is used in "Upsource Integration" plugin
  @NotNull
  public P4Connection getConnection() {
    return myConnection;
  }
}
