package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.CollectionFactory;
import com.intellij.util.containers.HashingStrategy;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManagerI;

import java.io.File;
import java.util.Collection;

public final class FileGrouper {
  private static <T> MultiMap<P4Connection, T> distributeByConnection(Collection<? extends T> files, NullableFunction<? super T, ? extends P4Connection> fun) {
    //todo P4ParametersConnection.equals should be based on client+server+user
    MultiMap<P4Connection, T> sortedFiles = new MultiMap<>(CollectionFactory.createCustomHashingStrategyMap(new HashingStrategy<>() {
      @Override
      public int hashCode(P4Connection object) {
        return object == null ? 0 : object.getConnectionKey().hashCode();
      }

      @Override
      public boolean equals(P4Connection o1, P4Connection o2) {
        return o1 == o2 || (o1 != null && o2 != null && Comparing.equal(o1.getConnectionKey(), o2.getConnectionKey()));
      }
    }));
    for (T dir : files) {
      P4Connection connection = fun.fun(dir);
      if (connection != null) {
        sortedFiles.putValue(connection, dir);
      }
    }
    return sortedFiles;
  }

  public static MultiMap<P4Connection, VirtualFile> distributeFilesByConnection(Collection<? extends VirtualFile> files, final Project project) {
    final PerforceConnectionManagerI mgr = PerforceConnectionManager.getInstance(project);
    return distributeByConnection(files, file -> mgr.getConnectionForFile(file));
  }

  public static MultiMap<P4Connection, FilePath> distributePathsByConnection(Collection<? extends FilePath> files, final Project project) {
    final PerforceConnectionManagerI connectionManager = PerforceConnectionManager.getInstance(project);
    return distributeByConnection(files, path -> connectionManager.getConnectionForFile(P4File.create(path)));
  }

  public static MultiMap<P4Connection, File> distributeIoFilesByConnection(final Collection<? extends File> files, final Project project) {
    final PerforceConnectionManagerI connectionManager = PerforceConnectionManager.getInstance(project);
    return distributeByConnection(files, file -> connectionManager.getConnectionForFile(file));
  }
}
