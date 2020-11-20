package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeImpl;
import com.intellij.openapi.vcs.changes.VcsIgnoreManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.MultiMap;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.idea.perforce.perforce.P4HaveParser;
import org.jetbrains.idea.perforce.perforce.PathsHelper;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.io.File;
import java.util.*;

/**
 * @author Irina Chernushina
 * @author peter
 */
public abstract class UnversionedScopeScanner {
  private static final Logger LOG = Logger.getInstance(UnversionedScopeScanner.class);
  private final Project myProject;
  private final PerforceRunner myRunner;

  public UnversionedScopeScanner(final Project project) {
    myProject = project;
    myRunner = PerforceRunner.getInstance(myProject);
  }

  protected abstract void checkCanceled();

  public ScanResult doRescan(Collection<FilePath> dirtyFiles, boolean force) throws VcsException {
    final ScanResult result = new ScanResult();
    if (dirtyFiles.isEmpty()) {
      return result;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("scope=" + dirtyFiles);
    }
    MultiMap<P4Connection,FilePath> map = FileGrouper.distributePathsByConnection(dirtyFiles, myProject);
    for (P4Connection connection : map.keySet()) {
      Collection<FilePath> files = map.get(connection);

      final Set<VirtualFile> localFiles = enumerateLocalFiles(files);
      result.allLocalFiles.addAll(localFiles);
      checkCanceled();

      final LocalFileSystem fs = LocalFileSystem.getInstance();
      myRunner.haveMultiple(collectPaths(files), connection, new P4HaveParser(PerforceManager.getInstance(myProject)) {
        @Override
        public void consumeRevision(String path, long revision) {
          checkCanceled();

          VirtualFile vFile = findVirtualFile(path);
          if (vFile != null) {
            localFiles.remove(vFile);
          }
          else {
            if (LOG.isDebugEnabled()) {
              LOG.debug("locally missing file reported: " + path);
            }
            result.missingFiles.add(path);
          }
        }

        private VirtualFile findVirtualFile(String path) {
          VirtualFile vFile = fs.findFileByPathIfCached(FileUtil.toSystemIndependentName(path));
          if (vFile != null) {
            return vFile;
          }

          // see https://youtrack.jetbrains.com/issue/IDEA-39796
          File ioFile = new File(path);
          String appleForkPath = FileUtil.toSystemIndependentName(ioFile.getParent()) + "/%" + ioFile.getName();
          return fs.findFileByPathIfCached(FileUtil.toSystemIndependentName(appleForkPath));
        }
      });
      checkCanceled();

      if (force) {
        result.localOnly.addAll(localFiles);
      }
      else {
        ChangeListManager clm = ChangeListManager.getInstance(myProject);
        for (VirtualFile file : localFiles) {
          boolean ignored = clm.isIgnoredFile(file);
          if (LOG.isDebugEnabled()) {
            LOG.debug("localOnly reported: " + file + (ignored ? ", ignored" : ""));
          }
          if (!ignored) {
            result.localOnly.add(file);
          }
        }
      }
    }
    return result;
  }

  private PathsHelper collectPaths(Collection<FilePath> value) {
    final PathsHelper helper = new PathsHelper(PerforceManager.getInstance(myProject));
    for (final FilePath dir : value) {
      if (dir.isDirectory()) {
        helper.addRecursively(dir);
      }
      else {
        helper.add(dir);
      }
    }
    return helper;
  }

  static List<FilePath> createEverythingDirtyScope(Project project) throws VcsException {
    List<FilePath> scope = new ArrayList<>();
    for (Pair<P4Connection, Collection<VirtualFile>> pair : PerforceVcs.getInstance(project).getRootsByConnections()) {
      for (VirtualFile file : pair.second) {
        scope.add(VcsUtil.getFilePath(file));
      }
    }
    return scope;
  }

  static class ScanResult {
    final Set<String> missingFiles = new HashSet<>();
    final List<VirtualFile> localOnly = new ArrayList<>();
    final Set<VirtualFile> allLocalFiles = new HashSet<>();
  }

  private Set<VirtualFile> enumerateLocalFiles(Collection<FilePath> roots) {
    final VcsDirtyScopeImpl scope = new VcsDirtyScopeImpl(PerforceVcs.getInstance(myProject));
    for (final FilePath root : roots) {
      scope.addDirtyDirRecursively(root);
    }

    final Set<VirtualFile> localFiles = new HashSet<>();
    scope.iterateExistingInsideScope(file -> {
      if (!file.isDirectory() && !skipPotentiallyIgnored(file)) {
        localFiles.add(file);
      } else {
        checkCanceled();
      }
      return true;
    });
    return localFiles;
  }

  private boolean skipPotentiallyIgnored(VirtualFile file) {
    return Registry.is("p4.ignore.all.potentially.ignored") && VcsIgnoreManager.getInstance(myProject).isPotentiallyIgnoredFile(file);
  }

}
