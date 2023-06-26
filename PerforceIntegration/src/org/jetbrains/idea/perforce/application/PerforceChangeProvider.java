package org.jetbrains.idea.perforce.application;

import com.google.common.base.Stopwatch;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.idea.perforce.perforce.*;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManagerI;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PerforceChangeProvider implements ChangeProvider {
  private static final Logger LOG = Logger.getInstance(PerforceChangeProvider.class);
  private static final Logger REFRESH_LOG = Logger.getInstance("#PerforceRefresh");

  public PerforceUnversionedTracker getUnversionedTracker() {
    return myUnversionedTracker;
  }

  private final Project myProject;
  private final PerforceRunner myRunner;
  private final LastSuccessfulUpdateTracker myLastSuccessfulUpdateTracker;
  private final PerforceNumberNameSynchronizer mySynchronizer;
  private final PerforceReadOnlyFileStateManager myPerforceReadOnlyFileStateManager;

  private final PerforceDirtyFilesHandler myDirtyFilesHandler;
  private final PerforceUnversionedTracker myUnversionedTracker;
  private final Map<VirtualFile, Boolean> myAlwaysWritable = new ConcurrentHashMap<>();
  private final PerforceShelf myShelf;
  private final PerforceVcs myVcs;

  public PerforceChangeProvider(final PerforceVcs vcs) {
    myVcs = vcs;
    myProject = vcs.getProject();
    myRunner = PerforceRunner.getInstance(myProject);
    myLastSuccessfulUpdateTracker = LastSuccessfulUpdateTracker.getInstance(myProject);
    mySynchronizer = PerforceNumberNameSynchronizer.getInstance(myProject);
    myUnversionedTracker = new PerforceUnversionedTracker(myProject);
    myDirtyFilesHandler = new PerforceDirtyFilesHandler(myProject, myUnversionedTracker);
    myPerforceReadOnlyFileStateManager = new PerforceReadOnlyFileStateManager(myProject, myDirtyFilesHandler);
    myShelf = PerforceManager.getInstance(myProject).getShelf();
  }

  public void activate(@NotNull Disposable parentDisposable) {
    myPerforceReadOnlyFileStateManager.activate(parentDisposable);
    myDirtyFilesHandler.activate(parentDisposable);
  }

  @Override
  public void getChanges(@NotNull final VcsDirtyScope dirtyScope, @NotNull final ChangelistBuilder builder, @NotNull final ProgressIndicator progress,
                         @NotNull final ChangeListManagerGate addGate) throws VcsException {
    Stopwatch sw = Stopwatch.createStarted();
    try (AccessToken ignored = myVcs.readLockP4()) {
      doGetChanges(dirtyScope, builder, progress, addGate);
    }
    sw.stop();
    REFRESH_LOG.info("getChanges took %d s".formatted(sw.elapsed().toSeconds()));
  }

  private void doGetChanges(@NotNull VcsDirtyScope dirtyScope,
                            @NotNull ChangelistBuilder builder,
                            @NotNull ProgressIndicator progress,
                            @NotNull ChangeListManagerGate addGate) throws VcsException {
    logRefreshDebug("getting changes for scope " + dirtyScope);

    myLastSuccessfulUpdateTracker.updateStarted();
    myShelf.clearShelf();
    PerforceManager.getInstance(myProject).clearInfoClientCache();

    PerforceChangeCache changeCache = new PerforceChangeCache(myProject);
    MultiMap<ConnectionKey, PerforceChangeList> allLists = calcChangeListMap(changeCache);
    refreshSynchronizer(addGate, allLists);

    ChangeCreator creator = new ChangeCreator(myProject);

    final Map<ConnectionKey, P4Connection> key2connection = PerforceSettings.getSettings(myProject).getConnectionsByKeys();
    MultiMap<ConnectionKey, VirtualFile> roots = getAffectedRoots(dirtyScope);
    for (ConnectionKey key : roots.keySet()) {
      P4Connection connection = key2connection.get(key);
      if (connection != null) {
        processConnection(connection, builder, roots.get(key), progress, addGate, allLists.get(key), dirtyScope, changeCache, creator);
      }
    }

    Stopwatch sw = Stopwatch.createStarted();
    myPerforceReadOnlyFileStateManager.getChanges(dirtyScope, builder, progress, addGate);
    sw.stop();
    logRefreshDebug("readOnlyFileStateManager.getChanges took %d s".formatted(sw.elapsed().toSeconds()));

    final Set<VirtualFile> writableFiles = collectWritableFiles(dirtyScope, false);

    for (VirtualFile file : PerforceVcs.getInstance(myProject).getAsyncEditedFiles()) {
      if (writableFiles.contains(file)) {
        processAsyncEdit(file, builder, creator);
        writableFiles.remove(file);
      }
    }
    reportModifiedWithoutCheckout(builder, creator, writableFiles);
    myLastSuccessfulUpdateTracker.updateSuccessful();
  }

  private void reportModifiedWithoutCheckout(ChangelistBuilder builder, ChangeCreator creator, Set<VirtualFile> writableFiles) throws VcsException {
    Stopwatch sw = Stopwatch.createStarted();

    List<VirtualFile> unknown = new ArrayList<>();
    for (VirtualFile file : writableFiles) {
      if (!myUnversionedTracker.isLocalOnly(file)) {
        if (!creator.reportedChanges.contains(file)) {
          Boolean alwaysWritable = myAlwaysWritable.get(file);
          if (alwaysWritable == Boolean.FALSE) {
            logDebug("reportModifiedWithoutCheckout, hijacked file = " + file);
            builder.processModifiedWithoutCheckout(file);
          } else if (alwaysWritable == null) {
            logDebug("reportModifiedWithoutCheckout, unknown file = " + file);
            unknown.add(file);
          }
        }
      }
    }

    if (!unknown.isEmpty() && SystemProperties.getBooleanProperty("perforce.always.writable.check.enabled", true)) {
      MultiMap<P4Connection, VirtualFile> map = FileGrouper.distributeFilesByConnection(unknown, myProject);
      for (P4Connection connection : map.keySet()) {
        for (VirtualFile file : getHijackedFiles(map, connection)) {
          builder.processModifiedWithoutCheckout(file);
        }
      }
    }

    sw.stop();
    logRefreshDebug("reportModifiedWithoutCheckout took %d".formatted(sw.elapsed().toSeconds()));
  }

  private List<VirtualFile> getHijackedFiles(MultiMap<P4Connection, VirtualFile> map, P4Connection connection) throws VcsException {
    if (isAllWriteWorkspace(connection, myProject)) {
      return Collections.emptyList();
    }

    List<VirtualFile> files = new ArrayList<>(map.get(connection));
    List<String> paths = ContainerUtil.map(files, file -> P4File.escapeWildcards(file.getPath()));
    List<String> output = myRunner.files(paths, connection);

    List<VirtualFile> hijacked = new ArrayList<>();
    int fileIndex = 0;
    for (String line : output) {
      if (StringUtil.isEmptyOrSpaces(line)) continue;
      if (fileIndex >= files.size()) break;

      int lParen = line.lastIndexOf('(');
      if (lParen < 0) continue;

      VirtualFile file = files.get(fileIndex);
      logDebug("getHijackedFiles, checking file = " + file + "; line = " + line);
      boolean expectedWritable = line.substring(lParen).contains("+w");
      myAlwaysWritable.put(file, expectedWritable);
      if (!expectedWritable) {
        hijacked.add(file);
      }

      fileIndex++;
    }
    return hijacked;
  }

  public static boolean isAllWriteWorkspace(@NotNull P4Connection connection, @NotNull Project project) {
    List<String> options = PerforceManager.getInstance(project).getClient(connection).getCachedOptions();
    //noinspection SpellCheckingInspection
    return options != null && options.contains("allwrite");
  }

  private static void logDebug(final String message) {
    LOG.debug(message);
  }

  private static void logRefreshDebug(final String message) {
    REFRESH_LOG.debug(message);
  }

  private MultiMap<ConnectionKey, VirtualFile> getAffectedRoots(VcsDirtyScope dirtyScope) throws VcsException {
    final PerforceConnectionManagerI connectionManager = PerforceConnectionManager.getInstance(myProject);
    MultiMap<ConnectionKey, VirtualFile> roots = new MultiMap<>();
    for (VirtualFile root : dirtyScope.getAffectedContentRoots()) {
      P4Connection connection = connectionManager.getConnectionForFile(root);
      if (connection != null) {
        PerforceManager.ensureValidClient(myProject, connection);
        ConnectionKey key = connection.getConnectionKey();
        roots.putValue(key, root);
      }
    }
    return roots;
  }

  private boolean shouldShowChangeList(final PerforceChangeList pcl, Collection<VirtualFile> allRoots, P4Connection connection) {
    if (pcl.getChanges().isEmpty()) {
      return !mySynchronizer.isHidden(pcl.getNumber());
    }
    if (myShelf.hasLocalChanges(connection.getConnectionKey(), pcl.getNumber())) {
      return true;
    }
    return ContainerUtil.or(allRoots, root -> !pcl.getChangesUnder(root).isEmpty());
  }

  private MultiMap<ConnectionKey, PerforceChangeList> calcChangeListMap(PerforceChangeCache changeCache) throws VcsException {
    final MultiMap<ConnectionKey, PerforceChangeList> allLists = MultiMap.create();
    for (Pair<P4Connection, Collection<VirtualFile>> pair : PerforceVcs.getInstance(myProject).getRootsByConnections()) {
      final P4Connection connection = pair.first;
      PerforceManager.ensureValidClient(myProject, connection);
      allLists.putValues(connection.getConnectionKey(), getPendingChangeListsUnderRoots(changeCache, connection, pair.second));
    }
    return allLists;
  }

  private List<PerforceChangeList> getPendingChangeListsUnderRoots(PerforceChangeCache changeCache, P4Connection connection, Collection<VirtualFile> allRoots) throws VcsException {
    List<PerforceChangeList> perforceLists = myRunner.getPendingChangeLists(connection, changeCache);
    if (perforceLists.isEmpty()) return perforceLists;

    myRunner.fillChangeCache(connection, changeCache, myShelf, perforceLists);

    List<PerforceChangeList> filtered = new ArrayList<>();
    for (final PerforceChangeList pcl : perforceLists) {
      if (shouldShowChangeList(pcl, allRoots, connection)) {
        mySynchronizer.setHidden(pcl.getNumber(), false);
        filtered.add(pcl);
      }
    }

    return filtered;
  }

  static Set<VirtualFile> collectWritableFiles(VcsDirtyScope dirtyScope, boolean withIgnored) {
    Stopwatch sw = Stopwatch.createStarted();
    final Set<VirtualFile> writableFiles = new HashSet<>();
    dirtyScope.iterateExistingInsideScope(vf -> {
      ApplicationManager.getApplication().runReadAction(() -> {
        if (vf.isValid() && !vf.isDirectory() && vf.isWritable() && !vf.is(VFileProperty.SYMLINK)) {
          if (withIgnored || !ChangeListManager.getInstance(dirtyScope.getProject()).isIgnoredFile(vf)) {
            writableFiles.add(vf);
          }
        }
      });
      return true;
    });

    sw.stop();
    logRefreshDebug("collected %d writable files in %d seconds".formatted(writableFiles.size(), sw.elapsed().toSeconds()));
    return writableFiles;
  }

  private void processAsyncEdit(VirtualFile file, ChangelistBuilder builder, ChangeCreator changeCreator)
    throws VcsException {
    long revision = myRunner.haveRevision(P4File.create(file));
    if (revision > 0) {
      // todo: future optimization point
      final FilePath filePath = VcsContextFactory.getInstance().createFilePathOn(file);
      builder.processChange(changeCreator.createEditedFileChange(filePath, revision, false), PerforceVcs.getKey());
    }
  }

  private void refreshSynchronizer(final ChangeListManagerGate addGate, final MultiMap<ConnectionKey, PerforceChangeList> allLists) {
    for (final ConnectionKey key : allLists.keySet()) {
      addGate.setListsToDisappear(mySynchronizer.acceptInfo(key, allLists.get(key), addGate));
    }
    mySynchronizer.removeNonexistentKeys(allLists.keySet());
  }

  private void processConnection(@NotNull final P4Connection connection,
                                 final ChangelistBuilder builder,
                                 final Collection<VirtualFile> roots,
                                 final ProgressIndicator progress,
                                 final ChangeListManagerGate addGate,
                                 final Collection<PerforceChangeList> allLists,
                                 final VcsDirtyScope dirtyScope,
                                 PerforceChangeCache changeCache, ChangeCreator changeCreator) throws VcsException {
    progress.checkCanceled();
    Stopwatch sw = Stopwatch.createStarted();

    final LocalPathsSet resolvedWithConflictsMap = myRunner.getResolvedWithConflictsMap(connection, roots);
    final ResolvedFilesWrapper resolvedFilesWrapper = new ResolvedFilesWrapper(myRunner.getResolvedFiles(connection, roots));

    final List<PerforceChange> changes = new ArrayList<>();
    for (VirtualFile root : roots) {
      changes.addAll(getChangesUnder(connection, root, dirtyScope, allLists, changeCache));
    }

    final PerforceChangeListCalculator changeListCalculator = new PerforceChangeListCalculator(myProject, connection, addGate);

    final OpenedResultProcessor processor =
      new OpenedResultProcessor(connection, changeCreator, builder, resolvedWithConflictsMap, resolvedFilesWrapper,
                                changeListCalculator);
    processor.process(changes);

    sw.stop();
    logRefreshDebug("processConnection %s took %d s".formatted(connection.getConnectionKey(), sw.elapsed().toSeconds()));
  }

  @Override
  public boolean isModifiedDocumentTrackingRequired() {
    return false;
  }

  public void discardCache() {
    myPerforceReadOnlyFileStateManager.discardUnversioned();
    myAlwaysWritable.clear();
  }

  @TestOnly
  public void imitateLostFocus() {
    myPerforceReadOnlyFileStateManager.processFocusLost();
  }

  private List<PerforceChange> getChangesUnder(final P4Connection connection, @NotNull final VirtualFile root,
                                               final VcsDirtyScope dirtyScope,
                                                     final Collection<PerforceChangeList> allLists, PerforceChangeCache changeCache) throws VcsException {

    final List<PerforceChange> perforceChanges = new ArrayList<>();

    List<PerforceChange> defChanges = filterByRoot(root, dirtyScope, changeCache.getChanges(connection, -1, root));
    if (!defChanges.isEmpty()) {
      myRunner.setChangeRevisionsFromHave(connection, defChanges);
      perforceChanges.addAll(defChanges);
    }

    for (PerforceChangeList changeList : allLists) {
      perforceChanges.addAll(filterByRoot(root, dirtyScope, changeCache.getChanges(connection, changeList.getNumber(), root)));
    }

    return perforceChanges;
  }

  private static List<PerforceChange> filterByRoot(VirtualFile root, final VcsDirtyScope dirtyScope, List<PerforceChange> perforceChanges) {
    final File ioRoot = new File(root.getPath());
    final List<PerforceChange> result = new ArrayList<>();
    for (PerforceChange perforceChange : perforceChanges) {
      File file = perforceChange.getFile();
      if (file != null && FileUtil.isAncestor(ioRoot, file, false) && dirtyScope.belongsTo(VcsUtil.getFilePath(file))) {
        result.add(perforceChange);
      }
    }
    return result;
  }

  public void clearUnversionedStatus(@NotNull FilePath file) {
    myDirtyFilesHandler.reportRecheck(file);
  }
}
