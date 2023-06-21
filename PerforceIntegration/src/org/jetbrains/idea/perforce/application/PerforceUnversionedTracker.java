package org.jetbrains.idea.perforce.application;

import com.google.common.base.Stopwatch;
import com.intellij.concurrency.ConcurrentCollectionFactory;
import com.intellij.dvcs.ignore.IgnoredToExcludedSynchronizer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.advanced.AdvancedSettings;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.util.BackgroundTaskUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.LocalFilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.update.ComparableObject;
import com.intellij.util.ui.update.DisposableUpdate;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.ServerVersion;
import org.jetbrains.idea.perforce.perforce.ExecResult;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.View;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class PerforceUnversionedTracker implements Disposable {
  private static final Logger LOG = Logger.getInstance(PerforceUnversionedTracker.class);
  private final Set<VirtualFile> myUnversionedFiles = ConcurrentCollectionFactory.createConcurrentSet();
  private final Set<VirtualFile> myIgnoredFiles = ConcurrentCollectionFactory.createConcurrentSet();

  private final Project myProject;
  private final static int ourFilesThreshold = 200;

  private boolean myTotalRescanThresholdPassed = true;
  private final Set<VirtualFile> myLocalDirtyFiles = new HashSet<>();
  private final Set<FilePath> myDirtyFiles = new HashSet<>();
  private final Object myScannerLock = new Object();
  private final VcsDirtyScopeManager myDirtyScopeManager;
  volatile boolean isActive;

  private final MergingUpdateQueue myQueue;
  private boolean myInUpdate;

  public PerforceUnversionedTracker(Project project) {
    myProject = project;
    myDirtyScopeManager = VcsDirtyScopeManager.getInstance(myProject);
    myDirtyScopeManager.markEverythingDirty();

    myQueue = VcsIgnoreManagerImpl.getInstanceImpl(myProject).getIgnoreRefreshQueue();
  }

  public void activate(@NotNull Disposable parentDisposable) {
    MessageBusConnection connection = myProject.getMessageBus().connect(parentDisposable);
    connection.subscribe(ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED, this::cancelAndRescheduleTotalRescan);
    connection.subscribe(PerforceSettings.OFFLINE_MODE_EXITED, this::cancelAndRescheduleTotalRescan);
  }

  public void cancelAndRescheduleTotalRescan() {
    isActive = false;
    scheduleTotalRescan();
  }

  public boolean isUnversioned(@NotNull VirtualFile file) {
    return myUnversionedFiles.contains(file);
  }

  public boolean isIgnored(@NotNull VirtualFile file) {
    return myIgnoredFiles.contains(file) || isPotentiallyIgnoredFile(file);
  }

  public Collection<VirtualFile> getIgnoredFiles() {
    return myIgnoredFiles;
  }

  public Collection<VirtualFile> getUnversionedFiles() {
    return myUnversionedFiles;
  }

  private final Object LOCK = new Object();

  public boolean isInUpdateMode() {
    synchronized (LOCK) {
      return myInUpdate;
    }
  }

  public void scheduleUpdate() {
    synchronized (LOCK) {
      if (myLocalDirtyFiles.isEmpty()) return;
      myInUpdate = true;
    }
    BackgroundTaskUtil.syncPublisher(myProject, VcsManagedFilesHolder.TOPIC).updatingModeChanged();
    myQueue.queue(DisposableUpdate.createDisposable(this, new ComparableObject.Impl(this, "update"), this::update));
  }

  private void update() {
    MultiMap<P4Connection, VirtualFile> map;
    synchronized (LOCK) {
      LOG.debug("update: " + myLocalDirtyFiles);
      if (myLocalDirtyFiles.size() == 0) {
        myInUpdate = false;
        return;
      }

      map = FileGrouper.distributeFilesByConnection(myLocalDirtyFiles, myProject);
      myLocalDirtyFiles.clear();
    }

    Set<VirtualFile> ignoredSet = new HashSet<>();
    try {
      for (P4Connection connection : map.keySet()) {
        ignoredSet.addAll(getFilesOutsideClientSpec(myProject, connection, map.get(connection)));
      }
    }
    catch (VcsException ignored) {

    }

    for (VirtualFile file : map.values()) {
      if (ignoredSet.contains(file)) {
        myIgnoredFiles.add(file);
      } else {
        myUnversionedFiles.add(file);
      }
    }

    synchronized (LOCK) {
      myInUpdate = false;
    }

    // todo: check if needed
    BackgroundTaskUtil.syncPublisher(myProject, VcsManagedFilesHolder.TOPIC).updatingModeChanged();
    ChangeListManagerImpl.getInstanceImpl(myProject).notifyUnchangedFileStatusChanged();
    // todo: refactor
    notifyExcludedSynchronizer(new HashSet<>(), myIgnoredFiles.stream().map(it -> new LocalFilePath(it.getPath(), it.isDirectory())).collect(
      Collectors.toList()));
  }

  public void markUnversioned(List<VirtualFile> files) {
    synchronized (LOCK) {
      myLocalDirtyFiles.clear();
      myLocalDirtyFiles.addAll(files);
    }
  }

  public void markUnknown(@NotNull Set<VirtualFile> files) {
    synchronized (LOCK) {
      files.forEach(myUnversionedFiles::remove);
      files.forEach(myIgnoredFiles::remove);
    }
  }

  public void markUnknown(@Nullable VirtualFile file) {
    if (file != null) {
      synchronized (LOCK) {
        myUnversionedFiles.remove(file);
        myIgnoredFiles.remove(file);
      }
    }
  }

  @Override
  public void dispose() {
    synchronized (LOCK) {
      myUnversionedFiles.clear();
      myIgnoredFiles.clear();
      myLocalDirtyFiles.clear();
    }
  }

  private static Set<VirtualFile> getExcludedFiles(Project project, P4Connection connection, Collection<VirtualFile> files) throws VcsException {
    PerforceClient client = PerforceManager.getInstance(project).getClient(connection);
    String clientName = client.getName();
    if (clientName == null) return Collections.emptySet();

    List<String> roots = ContainerUtil.map(client.getRoots(), FileUtil::toSystemIndependentName);
    List<View> views = client.getViews();
    return new LinkedHashSet<>(ContainerUtil.filter(files, f -> {
      String clientSpecPath = getClientSpecPath(f, clientName, roots);
      return clientSpecPath != null && View.isExcluded(clientSpecPath, views);
    }));
  }

  @Nullable
  private static String getClientSpecPath(VirtualFile file, String clientName, List<String> roots) {
    String path = file.getPath();
    for (String root : roots) {
      if (FileUtil.startsWith(path, root)) {
        return "//" + clientName + "/" + StringUtil.trimStart(path.substring(root.length()), "/");
      }
    }
    return null;
  }

  @NotNull
  private static Set<VirtualFile> getIgnoredFiles(Project project, P4Connection connection, List<VirtualFile> toCheckIgnored)
    throws VcsException{

    if (toCheckIgnored.isEmpty() || !AdvancedSettings.getBoolean("vcs.process.ignored")) {
      return Collections.emptySet();
    }

    ServerVersion serverVersion = PerforceManager.getInstance(project).getServerVersion(connection);

    if (serverVersion != null && serverVersion.supportsIgnoresCommand()) {
      return getIgnoredFilesByIgnores(project, connection, toCheckIgnored);
    }

    return getIgnoredFilesByPreviewAdd(project, connection, toCheckIgnored);
  }

  @NotNull
  private static Set<VirtualFile> getIgnoredFilesByPreviewAdd(Project project, P4Connection connection, List<VirtualFile> toCheckIgnored)
    throws VcsException {
    ExecResult execResult = PerforceRunner.getInstance(project).previewAdd(connection, toCheckIgnored);

    Set<VirtualFile> ignored = new LinkedHashSet<>();
    for (String line : execResult.getStderr().split("\n")) {
      if (line.isEmpty()) continue;
      if (line.contains("no permission for operation on file(s)")) continue;
      String path = StringUtil.trimEnd(line, PerforceRunner.NOT_IN_CLIENT_VIEW_MESSAGE + ".");
      if (path.equals(line)) {
        PerforceRunner.checkError(execResult, PerforceSettings.getSettings(project), null);
        continue;
      }
      ContainerUtil.addIfNotNull(ignored, VfsUtil.findFileByIoFile(new File(path), false));
    }
    for (String line : execResult.getStdout().split("\n")) {
      if (line.isEmpty()) continue;
      String path = StringUtil.trimEnd(line, ' ' + PerforceBundle.message("file.ignored.cannot.be.added"));
      if (path.equals(line)) {
        continue;
      }
      ContainerUtil.addIfNotNull(ignored, VfsUtil.findFileByIoFile(new File(path), false));
    }
    return ignored;
  }

  @NotNull
  private static Set<VirtualFile> getIgnoredFilesByIgnores(Project project, P4Connection connection, List<VirtualFile> toCheckIgnored) {
    var stopwatch = Stopwatch.createStarted();
    Set<VirtualFile> ignored = new LinkedHashSet<>();
    // 'p4 ignores' doesn't support '-x argfile', so we split manually
    for (List<VirtualFile> group : JBIterable.from(toCheckIgnored).split(100).map(JBIterable::toList)) {
      ExecResult execResult = PerforceRunner.getInstance(project).ignores(connection, group);

      if (execResult.getExitCode() == -1) {
        LOG.warn("P4 Ignores returned -1");
        continue;
      }

      for (String line : execResult.getStdout().split("\n")) {
        if (line.isEmpty())
          continue;

        String path = StringUtil.trimEnd(line, ' ' + PerforceBundle.message("file.ignored"));
        ContainerUtil.addIfNotNull(ignored, VfsUtil.findFileByIoFile(new File(path), false));
      }
    }


    stopwatch.stop();
    LOG.debug("P4 getChanges by ignores took %d s".formatted(stopwatch.elapsed().toSeconds()));
    return ignored;
  }

  public static Set<VirtualFile> getFilesOutsideClientSpec(Project project, P4Connection connection, Collection<VirtualFile> files)
    throws VcsException {
    files = ContainerUtil.filter(files, file -> !file.getPath().contains("..."));

    Set<VirtualFile> excluded = getExcludedFiles(project, connection, files);

    Set<VirtualFile> result = new LinkedHashSet<>(excluded);
    result.addAll(getIgnoredFiles(project, connection, ContainerUtil.filter(files, f -> !excluded.contains(f))));
    return result;
  }

  ThrowableComputable<UnversionedScopeScanner.ScanResult, VcsException> createScanner() {
    final UnversionedScopeScanner scanner = new UnversionedScopeScanner(myProject) {
      @Override
      protected void checkCanceled() {
        if (!isActive) {
          throw new ProcessCanceledException();
        }
      }
    };
    synchronized (myScannerLock) {
      if (myTotalRescanThresholdPassed) {
        myTotalRescanThresholdPassed = false;
        return () -> scanner.doRescan(UnversionedScopeScanner.createEverythingDirtyScope(myProject), true);
      }
      else {
        final Set<FilePath> dirtyFiles = new HashSet<>(myDirtyFiles);
        myDirtyFiles.clear();
        return () -> scanner.doRescan(dirtyFiles, false);
      }
    }
  }

  public void scheduleTotalRescan() {
    LOG.debug("totalRescan scheduled");
    synchronized (myScannerLock) {
      myTotalRescanThresholdPassed = true;
      myDirtyFiles.clear();
      myDirtyScopeManager.markEverythingDirty();
    }
  }

  private boolean addDirtyFile(FilePath holder) {
    synchronized (myScannerLock) {
      if (myTotalRescanThresholdPassed) {
        return false;
      }
      LOG.debug("addDirtyFile: " + holder);
      myDirtyFiles.add(holder);
      if (myDirtyFiles.size() > ourFilesThreshold) {
        scheduleTotalRescan();
        return false;
      }
      return true;
    }
  }

  void reportRecheck(final VirtualFile file) {
    markUnknown(file);
    if (addDirtyFile(VcsUtil.getFilePath(file))) {
      myDirtyScopeManager.fileDirty(file);
    }
  }

  void reportDelete(final VirtualFile file) {
    if (addDirtyFile(VcsUtil.getFilePath(file))) {
      myDirtyScopeManager.fileDirty(file);
    }
  }

  void reportRecheck(Set<VirtualFile> targets) {
    for (VirtualFile target : targets) {
      if (!addDirtyFile(VcsUtil.getFilePath(target))) {
        return;
      }
    }
    myDirtyScopeManager.filesDirty(targets, null);
  }

  private boolean isPotentiallyIgnoredFile(VirtualFile file) {
    return (Registry.is("p4.ignore.all.potentially.ignored") && VcsIgnoreManager.getInstance(myProject).isPotentiallyIgnoredFile(file));
  }

  // todo: check if needed
  private void notifyExcludedSynchronizer(@NotNull Set<FilePath> oldIgnored, @NotNull List<FilePath> newIgnored) {
    List<FilePath> addedIgnored = new ArrayList<>();
    for (FilePath filePath : newIgnored) {
      if (!oldIgnored.contains(filePath)) {
        addedIgnored.add(filePath);
      }
    }
    if (!addedIgnored.isEmpty()) {
      myProject.getService(IgnoredToExcludedSynchronizer.class).ignoredUpdateFinished(addedIgnored);
    }
  }
}
