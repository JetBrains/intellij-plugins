package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ChangeListManagerGate;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.idea.perforce.application.PerforceNumberNameSynchronizer;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yole
 */
public abstract class VcsOperation implements Cloneable {
  protected String myChangeList;

  @TestOnly
  public final void execute(final Project project) throws VcsException {
    execute(project, new ProcessingContext());
  }
  public abstract void execute(final Project project, ProcessingContext context) throws VcsException;

  protected VcsOperation() {
  }

  protected VcsOperation(String changeList) {
    myChangeList = changeList;
  }

  @NotNull abstract String getInputPath();

  @NotNull abstract String getOutputPath();

  public String getChangeList() {
    return myChangeList;
  }

  public void setChangeList(final String changeList) {
    myChangeList = changeList;
  }

  @Nullable
  public Change getChange(final Project project, ChangeListManagerGate addGate) {
    return null;
  }

  public void fillReopenedPaths(Map<String, String> result) {
  }

  /**
   * Checks if this operation modifies or reverts the specified operation.
   *
   * @param oldOp the operation to check for replacement.
   * @return null if this operation cancels {@code oldOp}; {@code oldOp} if the operations
   * are independent
   */
  @Nullable
  public VcsOperation checkMerge(final VcsOperation oldOp) {
    return oldOp;
  }

  protected long getPerforceChangeList(final Project project, final P4File p4File, ProcessingContext context) throws VcsException {
    final ChangeListManager listManager = ChangeListManager.getInstance(project);
    LocalChangeList list = listManager.findChangeList(myChangeList);
    if (list == null) {
      list = listManager.getDefaultChangeList();
    }
    P4Connection connection = PerforceConnectionManager.getInstance(project).getConnectionForFile(p4File);
    if (connection == null) {
      return -1;
    }

    for (Long number : PerforceNumberNameSynchronizer.getInstance(project).findOrCreate(connection, list)) {
      if (number > 0 && isValidPendingNumber(project, connection, number, context)) {
        return number;
      }
    }
    return -1;
  }

  private static final Key<Map<P4Connection, Map<Long, Boolean>>> PENDING_CACHE = Key.create("PENDING_CACHE");

  private static boolean isValidPendingNumber(Project project, P4Connection connection, long number, ProcessingContext context) {
    Map<P4Connection, Map<Long, Boolean>> map = context.get(PENDING_CACHE);
    if (map == null) {
      context.put(PENDING_CACHE, map = new HashMap<>());
    }

    Map<Long, Boolean> cache = map.computeIfAbsent(connection, k -> new HashMap<>());

    Boolean valid = cache.get(number);
    if (valid == null) {
      cache.put(number, valid = PerforceRunner.getInstance(project).isValidPendingNumber(connection, number));
    }

    return valid;
  }

  public void prepareOffline(Project project) {
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public abstract List<String> getAffectedPaths();
}
