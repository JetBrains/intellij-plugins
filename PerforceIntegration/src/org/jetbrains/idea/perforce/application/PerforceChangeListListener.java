package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.operations.P4MoveToChangeListOperation;
import org.jetbrains.idea.perforce.operations.VcsOperation;
import org.jetbrains.idea.perforce.operations.VcsOperationLog;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// it assumes it is not called on AWT thread
public class PerforceChangeListListener implements ChangeListListener {
  private static final Logger LOG = Logger.getInstance(PerforceChangeListListener.class);
  private final Project myProject;
  private final PerforceNumberNameSynchronizer mySynchronizer;

  public PerforceChangeListListener(Project project, PerforceNumberNameSynchronizer synchronizer) {
    myProject = project;
    mySynchronizer = synchronizer;
  }

  @Override
  public void changeListAdded(ChangeList list) {
    if (!(list instanceof LocalChangeList localChangeList)) {
      return;
    }

    final PerforceSettings settings = PerforceSettings.getSettings(myProject);
    if (!settings.ENABLED || !settings.FORCE_SYNC_CHANGELISTS) {
      return;
    }

    for(P4Connection connection: settings.getAllConnections()) {
      if (!checkConnection(connection, PerforceBundle.message("changelist.create.error")))
        continue;

      try {
        mySynchronizer.findOrCreate(connection, localChangeList);
      }
      catch (VcsException e) {
        AbstractVcsHelper.getInstance(myProject).showError(e, PerforceBundle.message("changelist.create.error"));
        LOG.info(e);
      }
    }
  }

  @Override
  public void changeListRemoved(final ChangeList list) {
    final String name = list.getName();
    applyToAllMatching(name, new MyCallback() {
      @Override
      public void call(Long number, P4Connection connection, ConnectionKey connectionKey) throws VcsException {
        PerforceRunner.getInstance(myProject).deleteChangeList(connection, number, true, false, true);

        mySynchronizer.removeList(number);
      }
    }, PerforceBundle.message("changelist.delete.error"));
  }

  private interface MyCallback {
    void call(final Long number, final P4Connection connection, ConnectionKey connectionKey) throws VcsException;
  }

  private void applyToAllMatching(final String listName, final MyCallback numberConsumer, @Nls String errorPrefix) {
    final PerforceSettings settings = PerforceSettings.getSettings(myProject);
    if (!settings.ENABLED) {
      return;
    }

    for(P4Connection connection: settings.getAllConnections()) {
      if (!checkConnection(connection, errorPrefix))
        continue;

      final ConnectionKey connectionKey = connection.getConnectionKey();

      Long number = mySynchronizer.getNumber(connectionKey, listName);
      if (number != null) {
        try {
          numberConsumer.call(number, connection, connectionKey);
        }
        catch (VcsException e) {
          AbstractVcsHelper.getInstance(myProject).showError(e, errorPrefix);
          LOG.info(e);
        }
      }
    }
  }

  @Override
  public void changeListRenamed(ChangeList list, String oldName) {
    handleChangeListRename(list, oldName);
  }

  @Override
  public void changeListCommentChanged(ChangeList list, String oldComment) {
    handleChangeListRename(list, list.getName());
  }

  private void handleChangeListRename(final ChangeList list, final String oldName) {
    final PerforceRunner runner = PerforceRunner.getInstance(myProject);
    applyToAllMatching(oldName, new MyCallback() {
      @Override
      public void call(Long number, P4Connection connection, ConnectionKey connectionKey) throws VcsException {
        runner.renameChangeList(number, getP4Description(list), connection);
      }
    }, PerforceBundle.message("changelist.rename.error"));

    final String newName = list.getName();
    if (! oldName.equals(newName)) {
      mySynchronizer.renameList(oldName, newName);
    }
  }

  private static String getP4Description(final ChangeList list) {
    String description = list.getComment().trim();
    if (description.isEmpty()) description = list.getName();
    return description;
  }

  @Override
  public void changesMoved(final Collection<? extends Change> changes, ChangeList fromList, final ChangeList toList) {
    final List<Change> changesUnderPerforce = new ArrayList<>();
    for (final Change change : changes) {
      AbstractVcs vcs = ChangesUtil.getVcsForChange(change, myProject);
      if (vcs != null && PerforceVcs.getKey().equals(vcs.getKeyInstanceMethod())) {
        changesUnderPerforce.add(change);
      }
    }

    List<VcsOperation> operations = new ArrayList<>();
    final String toListName = toList.getName();
    for(final Change change: changesUnderPerforce) {
      operations.add(new P4MoveToChangeListOperation(change, toListName));
    }
    VcsOperationLog.getInstance(myProject).queueOperations(operations, PerforceBundle.message("changelist.moving.to.another"),
                                                       PerformInBackgroundOption.ALWAYS_BACKGROUND);
  }

  private boolean checkConnection(P4Connection connection, @Nls String errorPrefix) {
    try {
      PerforceManager.ensureValidClient(myProject, connection);
    }
    catch (VcsException e) {
      AbstractVcsHelper.getInstance(myProject).showError(e, errorPrefix);
      LOG.info(e);
      return false;
    }

    return true;
  }
}
