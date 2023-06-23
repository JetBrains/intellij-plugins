package org.jetbrains.idea.perforce.application;

import com.google.common.base.Stopwatch;
import com.intellij.concurrency.ConcurrentCollectionFactory;
import com.intellij.dvcs.ignore.IgnoredToExcludedSynchronizer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.advanced.AdvancedSettings;
import com.intellij.openapi.progress.util.BackgroundTaskUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.LocalFilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeListManagerImpl;
import com.intellij.openapi.vcs.changes.VcsIgnoreManager;
import com.intellij.openapi.vcs.changes.VcsIgnoreManagerImpl;
import com.intellij.openapi.vcs.changes.VcsManagedFilesHolder;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.containers.MultiMap;
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
  private final Set<FilePath> myUnversionedFiles = ConcurrentCollectionFactory.createConcurrentSet();
  private final Set<FilePath> myIgnoredFiles = ConcurrentCollectionFactory.createConcurrentSet();
  private final Set<VirtualFile> myDirtyLocalFiles = ConcurrentCollectionFactory.createConcurrentSet();

  private final Project myProject;

  private final MergingUpdateQueue myQueue;
  private boolean myInUpdate;

  public PerforceUnversionedTracker(Project project) {
    myProject = project;
    myQueue = VcsIgnoreManagerImpl.getInstanceImpl(myProject).getIgnoreRefreshQueue();
  }

  public boolean isLocalOnly(@NotNull VirtualFile file) {
    if (myDirtyLocalFiles.contains(file))
      return true;
    FilePath path = VcsUtil.getFilePath(file);
    return isUnversioned(path) || isIgnored(path);
  }

  public boolean isUnversioned(@NotNull FilePath file) {
    return myUnversionedFiles.contains(file);
  }

  public boolean isIgnored(@NotNull FilePath file) {
    return myIgnoredFiles.contains(file) || isPotentiallyIgnoredFile(file);
  }

  public Collection<FilePath> getIgnoredFiles() {
    return myIgnoredFiles;
  }

  public Collection<FilePath> getUnversionedFiles() {
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
      if (myDirtyLocalFiles.isEmpty()) return;
      myInUpdate = true;
    }
    BackgroundTaskUtil.syncPublisher(myProject, VcsManagedFilesHolder.TOPIC).updatingModeChanged();
    myQueue.queue(DisposableUpdate.createDisposable(this, new ComparableObject.Impl(this, "update"), this::update));
  }

  private void update() {
    MultiMap<P4Connection, VirtualFile> map;
    Stopwatch sw = Stopwatch.createStarted();
    LOG.debug("update started for " + myDirtyLocalFiles.size() + " files");
    synchronized (LOCK) {
      if (myDirtyLocalFiles.size() == 0) {
        myInUpdate = false;
        return;
      }

      map = FileGrouper.distributeFilesByConnection(myDirtyLocalFiles, myProject);
    }

    Set<VirtualFile> ignoredSet = new HashSet<>();
    try {
      for (P4Connection connection : map.keySet()) {
        ignoredSet.addAll(getFilesOutsideClientSpec(myProject, connection, map.get(connection)));
      }
    }
    catch (VcsException e) {
      LOG.warn("Failed to get ignored files", e);
    }

    for (VirtualFile file : map.values()) {
      FilePath path = VcsUtil.getFilePath(file);
      if (ignoredSet.contains(file)) {
        myIgnoredFiles.add(path);
      } else {
        myUnversionedFiles.add(path);
      }
    }

    synchronized (LOCK) {
      myDirtyLocalFiles.clear();
      myInUpdate = false;
    }

    sw.stop();
    LOG.debug("update finished in %d seconds", sw.elapsed());

    // todo: check if needed
    BackgroundTaskUtil.syncPublisher(myProject, VcsManagedFilesHolder.TOPIC).updatingModeChanged();
    ChangeListManagerImpl.getInstanceImpl(myProject).notifyUnchangedFileStatusChanged();
    // todo: refactor
    notifyExcludedSynchronizer(new HashSet<>(), myIgnoredFiles.stream().map(it -> new LocalFilePath(it.getPath(), it.isDirectory())).collect(
      Collectors.toList()));
  }

  public void markUnversioned(List<VirtualFile> files) {
    myDirtyLocalFiles.addAll(files);
  }

  public void markUnknown(@NotNull Set<VirtualFile> files) {
    files.forEach(file -> {
      FilePath path = VcsUtil.getFilePath(file);
      myUnversionedFiles.remove(path);
      myIgnoredFiles.remove(path);
    });
  }

  public void markUnknown(@Nullable VirtualFile file) {
    if (file != null) {
      FilePath path = VcsUtil.getFilePath(file);
      myUnversionedFiles.remove(path);
      myIgnoredFiles.remove(path);
    }
  }

  @Override
  public void dispose() {
    synchronized (LOCK) {
      myUnversionedFiles.clear();
      myIgnoredFiles.clear();
      myDirtyLocalFiles.clear();
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
    Stopwatch sw = Stopwatch.createStarted();
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

    sw.stop();
    LOG.debug("checking %d ignored files by add preview took %d s".formatted(toCheckIgnored.size(), sw.elapsed().toSeconds()));

    return ignored;
  }

  @NotNull
  private static Set<VirtualFile> getIgnoredFilesByIgnores(Project project, P4Connection connection, List<VirtualFile> toCheckIgnored) {
    Stopwatch sw = Stopwatch.createStarted();
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

    sw.stop();
    LOG.debug("checking %d ignored files by p4 ignores took %d s".formatted(toCheckIgnored.size(), sw.elapsed().toSeconds()));
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

  private boolean isPotentiallyIgnoredFile(FilePath file) {
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
