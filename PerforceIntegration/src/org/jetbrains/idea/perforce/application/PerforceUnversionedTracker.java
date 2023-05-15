package org.jetbrains.idea.perforce.application;

import com.intellij.concurrency.ConcurrentCollectionFactory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.advanced.AdvancedSettings;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vcs.changes.VcsIgnoreManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.containers.MultiMap;
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

public class PerforceUnversionedTracker {
  private static final Logger LOG = Logger.getInstance(PerforceUnversionedTracker.class);
  private final Set<VirtualFile> myUnversionedFiles = ConcurrentCollectionFactory.createConcurrentSet();
  private final Set<VirtualFile> myIgnoredFiles = ConcurrentCollectionFactory.createConcurrentSet();
  private final Project myProject;
  private final static int ourFilesThreshold = 200;

  private boolean myTotalRescanThresholdPassed = true;
  private final Set<FilePath> myDirtyFiles = new HashSet<>();
  private final Object myScannerLock = new Object();
  private final VcsDirtyScopeManager myDirtyScopeManager;
  volatile boolean isActive;

  public PerforceUnversionedTracker(Project project) {
    myProject = project;
    myDirtyScopeManager = VcsDirtyScopeManager.getInstance(myProject);
    myDirtyScopeManager.markEverythingDirty();
  }

  public boolean isUnversioned(@NotNull VirtualFile file) {
    return myUnversionedFiles.contains(file);
  }

  public boolean isIgnored(@NotNull VirtualFile file) {
    return myIgnoredFiles.contains(file) || isPotentiallyIgnoredFile(file);
  }

  public void markUnversioned(List<VirtualFile> files) throws VcsException {
    LOG.debug("markUnversioned: " + files);

    MultiMap<P4Connection, VirtualFile> map = FileGrouper.distributeFilesByConnection(files, myProject);

    Set<VirtualFile> ignored = new HashSet<>();
    for (P4Connection connection : map.keySet()) {
      ignored.addAll(getFilesOutsideClientSpec(myProject, connection, map.get(connection)));
    }

    for (VirtualFile file : files) {
      if (ignored.contains(file)) {
        myIgnoredFiles.add(file);
      } else {
        myUnversionedFiles.add(file);
      }
    }
  }

  public void markUnknown(@Nullable VirtualFile file) {
    if (file != null) {
      myUnversionedFiles.remove(file);
      myIgnoredFiles.remove(file);
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
    throws VcsException {

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

    Set<VirtualFile> ignored = new LinkedHashSet<>();
    // 'p4 ignores' doesn't support '-x argfile', so we split manually
    for (List<VirtualFile> group : JBIterable.from(toCheckIgnored).split(100).map(JBIterable::toList)) {
      ExecResult execResult = PerforceRunner.getInstance(project).ignores(connection, group);

      for (String line : execResult.getStdout().split("\n")) {
        String path = StringUtil.trimEnd(line, ' ' + PerforceBundle.message("file.ignored"));
        ContainerUtil.addIfNotNull(ignored, VfsUtil.findFileByIoFile(new File(path), false));
      }
    }

    return ignored;
  }

  public static Set<VirtualFile> getFilesOutsideClientSpec(Project project, P4Connection connection, Collection<VirtualFile> files) throws VcsException {
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

  void totalRescan() {
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
        totalRescan();
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

}
