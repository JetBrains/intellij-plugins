package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.View;

import java.util.List;

public interface PerforceClient {

  @Nullable("in tests")
  Project getProject();

  @NlsSafe String getName() throws VcsException;

  /**
   * @return a list of the main root and all AltRoots
   */
  @NotNull
  List<String> getRoots() throws VcsException;

  List<View> getViews() throws VcsException;

  @Nullable
  List<String> getCachedOptions();

  @NlsSafe String getUserName() throws VcsException;

  /**
   * @return server+port taken from 'p4 info' output. Doesn't contain prefixes like 'ssl:', that can be retrieved from {@link #getDeclaredServerPort()}
   */
  @Nullable @NlsSafe String getServerPort() throws VcsException;

  /**
   * @return server+port taken from connection
   */
  @Nullable @NlsSafe
  String getDeclaredServerPort();
}
