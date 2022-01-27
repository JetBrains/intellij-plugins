package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsConnectionProblem;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.ProcessingContext;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.FileCollectionFactory;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager;

import java.io.File;
import java.util.*;

@State(name = "VcsOperationLog", storages = @Storage(StoragePathMacros.WORKSPACE_FILE), reportStatistic = false)
public final class VcsOperationLog implements PersistentStateComponent<VcsOperationLog.OperationList>{
  private static final Logger LOG = Logger.getInstance(VcsOperationLog.class);
  public static class OperationList {
    private final MultiMap<String, VcsOperation> myOperationsByOutputPath = new MultiMap<>(FileCollectionFactory.createCanonicalFilePathLinkedMap());
    @AbstractCollection(
      elementTypes = {
        P4AddOperation.class, P4CopyOperation.class, P4DeleteOperation.class, P4MoveRenameOperation.class, P4EditOperation.class,
        P4RevertOperation.class, P4MoveToChangeListOperation.class
      })
    public List<VcsOperation> getOperations() {
      return new ArrayList<>(myOperationsByOutputPath.values());
    }

    @AbstractCollection(
      elementTypes = {
        P4AddOperation.class, P4CopyOperation.class, P4DeleteOperation.class, P4MoveRenameOperation.class, P4EditOperation.class,
        P4RevertOperation.class, P4MoveToChangeListOperation.class
      })
    public void setOperations(@NotNull List<VcsOperation> operations) {
      myOperationsByOutputPath.clear();
      for (VcsOperation operation : operations) {
        addOperation(operation);
      }
    }

    private void addOperation(@NotNull VcsOperation operation) {
      myOperationsByOutputPath.putValue(operation.getOutputPath(), operation);
    }

    private void removeOperation(@NotNull VcsOperation operation) {
      myOperationsByOutputPath.remove(operation.getOutputPath(), operation);
    }

  }

  private final Object lock = new Object();
  private final Project myProject;
  private OperationList myOperations = new OperationList();

  public VcsOperationLog(final Project project) {
    myProject = project;
  }

  public static VcsOperationLog getInstance(Project project) {
    return project.getService(VcsOperationLog.class);
  }

  @Override
  public OperationList getState() {
    synchronized (lock) {
      return myOperations;
    }
  }

  @Override
  public void loadState(@NotNull OperationList state) {
    synchronized (lock) {
      myOperations = state;
    }
  }

  public void addToLog(final VcsOperation vcsOperation) {
    synchronized (lock) {
      for (VcsOperation oldOp : myOperations.myOperationsByOutputPath.get(vcsOperation.getInputPath())) {
        VcsOperation mergedOp = vcsOperation.checkMerge(oldOp);
        if (mergedOp != oldOp) {
          myOperations.removeOperation(oldOp);
          if (mergedOp != null) {
            myOperations.addOperation(mergedOp);
          }
          return;
        }
      }
      LOG.debug("Add to log " + vcsOperation);
      vcsOperation.prepareOffline(myProject);
      myOperations.addOperation(vcsOperation);
    }
  }

  public boolean runOperations(final List<VcsOperation> operations, final @NlsContexts.TabTitle String title, final PerformInBackgroundOption option, List<VcsException> exceptions) {
    Runnable runnable = enqueueOperations(operations, title, option, exceptions);
    if (runnable == null) return false;
    int startSize = exceptions.size();
    runnable.run();
    return startSize == exceptions.size();

  }
  public void queueOperations(final List<? extends VcsOperation> operations, final @NlsContexts.ProgressTitle String title, final PerformInBackgroundOption option) {
    List<VcsException> exceptions = new ArrayList<>();
    Runnable runnable = enqueueOperations(operations, title, option, exceptions);

    if (runnable == null) return;
    PerforceVcs.getInstance(myProject).runBackgroundTask(title, option, runnable);
  }

  @Nullable
  private Runnable enqueueOperations(List<? extends VcsOperation> operations,
                                     final @NlsContexts.TabTitle String title,
                                     final PerformInBackgroundOption option,
                                     final List<VcsException> exceptions) {
    synchronized (lock) {
      for (VcsOperation operation : operations) {
        addToLog(operation);
      }
    }

    if (!PerforceSettings.getSettings(myProject).ENABLED) {
      return null;
    }

    return () -> {
      HashSet<P4Connection> authorized = new HashSet<>();
      while (true) {
        try (AccessToken ignored = PerforceVcs.getInstance(myProject).writeLockP4()) {
          if (!new MergedOperationExecutor(takeMergeableOperations(), title, option, authorized).executeOperations()) {
            break;
          }
        }
        catch (VcsException e) {
          exceptions.add(e);
        }
      }
      if (!exceptions.isEmpty()) {
        AbstractVcsHelper.getInstance(myProject).showErrors(exceptions, title);
      }
    };
  }

  public void replayLog() {
    String title = PerforceBundle.message("replaying.offline.operations");
    queueOperations(Collections.emptyList(), title, PerformInBackgroundOption.DEAF);
  }

  private List<VcsOperation> takeMergeableOperations() {
    synchronized (lock) {
      if (!myOperations.myOperationsByOutputPath.isEmpty()) {
        Collection<? extends VcsOperation> allOperations = myOperations.myOperationsByOutputPath.values();
        VcsOperation firstOp = allOperations.iterator().next();
        List<VcsOperation> mergeable = ContainerUtil.filter(allOperations, op -> op.getClass() == firstOp.getClass());
        mergeable.forEach(myOperations::removeOperation);
        return mergeable;
      }
      return Collections.emptyList();
    }
  }

  public List<VcsOperation> getPendingOperations() {
    synchronized (lock) {
      return myOperations.getOperations();
    }
  }

  /**
   * Returns a map from a file path to the name of the changelist in which the file was reopened while offline.
   * If the value is null, the file has been reverted while offline.
   *
   * @return map of path to changelist name or null
   */
  public Map<String, String> getReopenedPaths() {
    Map<String, String> result = new TreeMap<>();
    for(VcsOperation op: getPendingOperations()) {
      op.fillReopenedPaths(result);
    }
    return result;
  }

  private class MergedOperationExecutor {
    private final LinkedHashSet<VcsOperation> myRemaining;
    private final @NlsContexts.ProgressTitle String myTitle;
    private final PerformInBackgroundOption myOption;
    private final Set<P4Connection> myAuthorized;
    private final ProcessingContext myContext = new ProcessingContext();

    MergedOperationExecutor(List<VcsOperation> operations, @NlsContexts.ProgressTitle String title,
                            PerformInBackgroundOption option, Set<P4Connection> authorized) {
      myRemaining = new LinkedHashSet<>(operations);
      myTitle = title;
      myOption = option;
      myAuthorized = authorized;
    }

    @Nullable
    private LinkedHashMap<ThrowableRunnable<VcsException>, Collection<VcsOperation>> mergeOperations()
      throws VcsConnectionProblem {

      LinkedHashMap<ThrowableRunnable<VcsException>, Collection<VcsOperation>> result = new LinkedHashMap<>();

      MultiMap<P4Connection, VcsOperation> byConnection = new MultiMap<>();

      for (final VcsOperation operation : myRemaining) {
        Set<P4Connection> touchedConnections = ensureAuthorized(operation);
        if (touchedConnections == null) {
          return null;
        }

        if (touchedConnections.size() == 1) {
          byConnection.putValue(touchedConnections.iterator().next(), operation);
        }
        else {
          handleNonMergeableOperation(result, operation);
        }
      }

      if (myRemaining.iterator().next() instanceof P4RevertOperation) {
        for (final P4Connection connection : byConnection.keySet()) {
          mergeRevert(result, connection, byConnection.get(connection));
        }
      } else {
        for (VcsOperation operation : byConnection.values()) {
          handleNonMergeableOperation(result, operation);
        }
      }

      return result;
    }

    private void mergeRevert(LinkedHashMap<ThrowableRunnable<VcsException>, Collection<VcsOperation>> result,
                             @NotNull final P4Connection connection,
                             final Collection<VcsOperation> operations) {
      if (operations.size() == 1) {
        handleNonMergeableOperation(result, operations.iterator().next());
        return;
      }

      result.put(() -> {
        List<String> toRevert = new ArrayList<>();
        List<File> toDelete = new ArrayList<>();
        for (VcsOperation operation : operations) {
          ((P4RevertOperation)operation).prepareRevert(toRevert, toDelete);
        }
        PerforceRunner.getInstance(myProject).revertAll(toRevert, connection);
        P4RevertOperation.refreshAfterRevert(toRevert, toDelete, myProject);
      }, operations);
    }

    private void handleNonMergeableOperation(LinkedHashMap<ThrowableRunnable<VcsException>, Collection<VcsOperation>> result,
                                             final VcsOperation operation) {
      result.put(() -> operation.execute(myProject, myContext), Collections.singletonList(operation));
    }

    @Nullable
    private Set<P4Connection> ensureAuthorized(VcsOperation operation) throws VcsConnectionProblem {
      Set<P4Connection> touchedConnections = new HashSet<>();
      for (String path : operation.getAffectedPaths()) {
        P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(new File(path));
        if (connection == null) {
          return null;
        }
        touchedConnections.add(connection);
        if (myAuthorized.add(connection)) {
          PerforceLoginManager.getInstance(myProject).check(connection, true);
        }
      }
      return touchedConnections;
    }

    private void fixLater(final VcsConnectionProblem e) {
      LOG.info(e);
      ApplicationManager.getApplication().invokeLater(() -> {
        boolean inTests = ApplicationManager.getApplication().isUnitTestMode();
        if (e.attemptQuickFix(!inTests) && PerforceSettings.getSettings(myProject).ENABLED) {
          queueOperations(Collections.emptyList(), myTitle, myOption);
        }
        else {
          PerforceSettings.getSettings(myProject).disable(false);
        }
      });
    }

    private void pushBackOperations() {
      synchronized (lock) {
        myOperations.setOperations(ContainerUtil.newArrayList(ContainerUtil.concat(myRemaining, myOperations.getOperations())));
      }
    }

    private boolean executeOperations() throws VcsException {
      if (myRemaining.isEmpty()) {
        return false;
      }

      try {
        LinkedHashMap<ThrowableRunnable<VcsException>, Collection<VcsOperation>> map = mergeOperations();
        if (map == null) {
          pushBackOperations();
          AbstractVcsHelper.getInstance(myProject)
            .showError(new VcsException(PerforceBundle.message("error.can.not.execute.invalid.connection.settings", myTitle)),
                       myTitle);
          return false;
        }

        for (ThrowableRunnable<VcsException> composite : map.keySet()) {
          composite.run();
          myRemaining.removeAll(map.get(composite));
        }
      }
      catch (VcsConnectionProblem e) {
        pushBackOperations();
        fixLater(e);
        return false;
      }

      return true;
    }
  }
}
