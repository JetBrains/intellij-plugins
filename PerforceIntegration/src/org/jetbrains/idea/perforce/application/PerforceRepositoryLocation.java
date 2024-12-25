package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.DefaultRepositoryLocation;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.io.File;

public final class PerforceRepositoryLocation extends DefaultRepositoryLocation {
  private final Project myProject;
  private P4Connection myConnection;
  private PerforceClient myClient;

  private PerforceRepositoryLocation(@NotNull String URL, final @Nullable P4Connection connection, final @Nullable Project project) {
    super(URL);
    assert (connection != null) || (project != null);
    myConnection = connection;
    myProject = project;
  }

  private PerforceRepositoryLocation(@NotNull String URL, final String location, final @Nullable P4Connection connection, final @Nullable Project project) {
    super(URL, location);
    assert (connection != null) || (project != null);
    myConnection = connection;
    myProject = project;
  }

  public static @NotNull PerforceRepositoryLocation create(@NotNull VirtualFile file, Project project) {
    return new PerforceRepositoryLocation(file.getPath(), null, project);
  }

  public static @NotNull PerforceRepositoryLocation create(@NotNull FilePath p, String location, Project project) {
    return new PerforceRepositoryLocation(p.getPath(), location, null, project);
  }

  @Override
  public void onAfterBatch() {
    myConnection = null;
    myClient = null;
  }

  @Override
  public void onBeforeBatch() throws VcsException {
    myConnection = getConnection();
    myClient = getClient();
  }

  /*public static PerforceRepositoryLocation createWithConnectionCaching(final String URL, final Project project) throws VcsException {
    return new PerforceRepositoryLocation(URL, getConnection(project, URL), project);
  }

  public static PerforceRepositoryLocation createWithConnectionCaching(final String URL, final String location, final Project project)
    throws VcsException {
    return new PerforceRepositoryLocation(URL, location, getConnection(project, URL), project);
  }*/

  private static @NotNull P4Connection getConnection(final Project project, final String url) throws VcsException {
    final P4Connection connection = PerforceConnectionManager.getInstance(project).getConnectionForFile(new File(url));
    if (connection == null) {
      throw new VcsException(PerforceBundle.message("error.can.not.get.p4.connection", url));
    }
    return connection;
  }

  public @NotNull P4Connection getConnection() throws VcsException {
    if (myConnection != null) {
      return myConnection;
    }
    return getConnection(myProject, getURL());
  }

  public @NotNull PerforceClient getClient() throws VcsException {
    if (myClient == null) {
      return createClient(getConnection());
    }
    return myClient;
  }

  private @NotNull PerforceClient createClient(final @NotNull P4Connection connection) {
    return PerforceManager.getInstance(myProject).getClient(connection);
  }
}
