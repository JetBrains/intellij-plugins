package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.RepositoryLocation;
import com.intellij.openapi.vcs.TreeDiffProvider;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.FilePathsHelper;
import com.intellij.openapi.vcs.changes.committed.CommittedChangesCache;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PerforceTreeDiffProvider implements TreeDiffProvider {
  private static final Logger LOG = Logger.getInstance(PerforceTreeDiffProvider.class);
  private final PerforceVcs myVcs;

  public PerforceTreeDiffProvider(final PerforceVcs vcs) {
    myVcs = vcs;
  }

  @Override
  public Collection<String> getRemotelyChanged(VirtualFile vcsRoot, Collection<String> paths) {
    final RepositoryLocation location =
      CommittedChangesCache.getInstance(myVcs.getProject()).getLocationCache().getLocation(myVcs, VcsUtil.getFilePath(vcsRoot), true);
    final PerforceCommittedChangesProvider committedChangesProvider = myVcs.getCommittedChangesProvider();
    try {
      final Collection<FilePath> files = committedChangesProvider.getIncomingFiles(location);
      final Set<String> helper = new HashSet<>();
      for (FilePath file : files) {
        final String convertedPath = FilePathsHelper.convertPath(file);
        helper.add(convertedPath);
      }

      final Collection<String> result = new HashSet<>();
      for (String path : paths) {
        final String convertedPath = FilePathsHelper.convertPath(path);
        if (helper.contains(convertedPath)) {
          result.add(path);
        }
      }
      return result;
    }
    catch (VcsException e) {
      LOG.info(e);
      return Collections.emptyList();
    }
  }
}
