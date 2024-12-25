package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.PerforceAbstractChange;
import org.jetbrains.idea.perforce.perforce.PerforceContentRevision;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.io.File;

public class ShelvedChange extends PerforceAbstractChange {
  private final String myDepotPath;
  private final long myRevision;
  private final long myChangeList;
  private final P4Connection myConnection;

  public ShelvedChange(int type, @NotNull String depotPath, long revision, @NotNull P4Connection connection, @Nullable File localFile, long changeList) {
    myDepotPath = depotPath;
    myRevision = revision;
    myConnection = connection;
    myChangeList = changeList;
    myType = type;
    setFile(localFile);
  }

  public @NotNull String getDepotPath() {
    return myDepotPath;
  }

  @Override
  public String toString() {
    return "ShelvedChange{" +
           "myRevision=" + myRevision +
           ", myDepotPath='" + myDepotPath + '\'' +
           '}';
  }

  public Change toIdeaChange(Project project) {
    PerforceContentRevision beforeRevision = myType == PerforceAbstractChange.ADD || myType == PerforceAbstractChange.MOVE_ADD 
                                             ? null 
                                             : PerforceContentRevision.create(project, myConnection, myDepotPath, myRevision, -1);
    PerforceContentRevision afterRevision = myType == PerforceAbstractChange.DELETE || myType == PerforceAbstractChange.MOVE_DELETE
                                            ? null
                                            : PerforceContentRevision.create(project, myConnection, myDepotPath, -1, myChangeList);
    return new IdeaChange(beforeRevision, afterRevision, this);
  }

  public long getChangeList() {
    return myChangeList;
  }

  public P4Connection getConnection() {
    return myConnection;
  }

  public static class IdeaChange extends Change {
    private final ShelvedChange myOriginal;

    public IdeaChange(@Nullable ContentRevision beforeRevision,
                      @Nullable ContentRevision afterRevision, 
                      ShelvedChange original) {
      super(beforeRevision, afterRevision);
      myOriginal = original;
    }

    public ShelvedChange getOriginal() {
      return myOriginal;
    }
  }
}
