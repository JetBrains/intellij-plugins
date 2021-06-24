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

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.operations.P4AddOperation;
import org.jetbrains.idea.perforce.operations.P4DeleteOperation;
import org.jetbrains.idea.perforce.operations.VcsOperation;
import org.jetbrains.idea.perforce.operations.VcsOperationLog;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceChange;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.jobs.PerforceCheckinComponent;
import org.jetbrains.idea.perforce.perforce.jobs.PerforceJob;

import java.util.*;

public class PerforceCheckinEnvironment implements CheckinEnvironment{
  private static final Logger LOG = Logger.getInstance(PerforceCheckinEnvironment.class);
  public static final Key<List<PerforceJob>> LINKED_JOBS_KEY = Key.create("perforce_linked_jobs");

  private final Project myProject;
  private final PerforceRunner myRunner;
  private final PerforceVcs myVcs;

  public PerforceCheckinEnvironment(Project project, PerforceVcs perforceVcs) {
    myProject = project;
    myRunner = PerforceRunner.getInstance(project);
    myVcs = perforceVcs;
  }

  @Nullable
  @Override
  public RefreshableOnComponent createCommitOptions(@NotNull CheckinProjectPanel commitPanel, @NotNull CommitContext commitContext) {
    return PerforceSettings.getSettings(myProject).USE_PERFORCE_JOBS ? new PerforceCheckinComponent(myProject, commitContext) : null;
  }

  @Override
  public String getHelpId() {
    return null;
  }

  @Override
  public String getCheckinOperationName() {
    return PerforceBundle.message("operation.name.submit");
  }

  @NotNull
  @Override
  public List<VcsException> commit(@NotNull List<? extends Change> changes,
                                   @NotNull String commitMessage,
                                   @NotNull CommitContext commitContext,
                                   @NotNull Set<? super String> feedback) {
    ArrayList<VcsException> vcsExceptions = new ArrayList<>();
    try (AccessToken ignored = myVcs.writeLockP4()) {
      final List<SubmitJob> map = getSubmitJobs(changes);
      if (map.isEmpty()) {
        vcsExceptions.add(new VcsException(PerforceBundle.message("exception.text.nothing.found.to.submit")));
      } else {
        for (SubmitJob job : map) {
          long submittedRevision = job.submit(commitMessage, getLinkedJobs(commitContext));
          if (submittedRevision > 0) {
            feedback.add("Perforce revision #" + submittedRevision);
          }
        }
      }

      LOG.info("updating opened files after commit");
    }
    catch (VcsException e) {
      vcsExceptions.add(e);
    }
    return vcsExceptions;
  }

  @Nullable
  private static List<PerforceJob> getLinkedJobs(@NotNull CommitContext commitContext) {
    return commitContext.getUserData(LINKED_JOBS_KEY);
  }

  public List<SubmitJob> getSubmitJobs(Collection<? extends Change> changes) throws VcsException {
    SplitListIntoConnections<SubmitJob> splitter = new SplitListIntoConnections<>(myProject, connection -> new SubmitJob(connection));
    splitter.execute(changes);
    final MultiMap<ConnectionKey, FilePath> filePaths = splitter.getPaths();
    final Map<ConnectionKey, SubmitJob> byConnectionMap = splitter.getByConnectionMap();

    for (ConnectionKey key : filePaths.keySet()) {
      final SubmitJob job = byConnectionMap.get(key);
      job.addChanges(myRunner.opened(job.getConnection(), filePaths.get(key), false));
    }
    return new ArrayList<>(byConnectionMap.values());
  }

  public class SubmitJob {
    private final P4Connection myConnection;
    private final List<PerforceChange> myChanges = new ArrayList<>();

    public SubmitJob(@NotNull final P4Connection connection) {
      myConnection = connection;
    }

    public void addChanges(final Collection<PerforceChange> changes) {
      myChanges.addAll(changes);
    }

    public P4Connection getConnection() {
      return myConnection;
    }

    public long submit(String comment, @Nullable List<PerforceJob> p4jobs) throws VcsException {
      if (myChanges.size() == 0) return -1;
      long changeListID = createSingleChangeListForConnection();
      long submittedRevision = myRunner.submitForConnection(myConnection, myChanges, changeListID, comment, p4jobs);
      if (changeListID == -1) {
        myVcs.clearDefaultAssociated();
      }
      return submittedRevision;
    }

    private long createSingleChangeListForConnection() throws VcsException {
      final MultiMap<Long, PerforceChange> byListMap = new MultiMap<>();
      for (PerforceChange change : myChanges) {
        byListMap.putValue(change.getChangeList(), change);
      }

      if (byListMap.size() == 0) return -1; //???
      if (byListMap.size() == 1) return byListMap.keySet().iterator().next();

      for (Long number : byListMap.keySet()) {
        if (number == -1) continue;
        myRunner.reopen(myConnection, -1, ContainerUtil.map(byListMap.get(number), change -> P4File.escapeWildcards(change.getDepotPath())));
        myRunner.deleteChangeList(myConnection, number, false, true, false);
      }
      return -1;
    }
  }

  @Override
  public List<VcsException> scheduleMissingFileForDeletion(final @NotNull List<? extends FilePath> files) {
    ChangeListManager manager = ChangeListManager.getInstance(myProject);
    ArrayList<VcsOperation> operations = new ArrayList<>();
    for (FilePath file : files) {
      Change change = manager.getChange(file);
      LocalChangeList list = change != null ? manager.getChangeList(change) : manager.getDefaultChangeList();
      operations.add(new P4DeleteOperation(list == null ? manager.getDefaultListName() : list.getName(), file));
    }
    VcsOperationLog.getInstance(myProject).queueOperations(operations, PerforceBundle.message("file.removing.files"),
                                                           PerformInBackgroundOption.ALWAYS_BACKGROUND);
    return Collections.emptyList();
  }

  @Override
  @NotNull
  public List<VcsException> scheduleUnversionedFilesForAddition(final @NotNull List<? extends VirtualFile> files) {
    String activeChangeList = ChangeListManager.getInstance(myProject).getDefaultChangeList().getName();
    final List<VcsOperation> ops = new ArrayList<>();
    for(VirtualFile file: files) {
      ops.add(new P4AddOperation(activeChangeList, file));
    }
    VcsOperationLog.getInstance(myProject).queueOperations(ops, PerforceBundle.message("progress.title.running.perforce.commands"),
                                                           PerformInBackgroundOption.ALWAYS_BACKGROUND);
    return Collections.emptyList();
  }

  @Override
  public boolean isRefreshAfterCommitNeeded() {
    return true;
  }

}
