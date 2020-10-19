package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.vcs.VcsException;

public interface PerforceRunnerI {
  FStat fstat(final P4File p4File) throws VcsException;
  void edit(P4File file) throws VcsException;
  void revert(final P4File p4File, final boolean justTry) throws VcsException;
  ExecResult sync(final P4File p4File, boolean forceSync) throws VcsException;
}
