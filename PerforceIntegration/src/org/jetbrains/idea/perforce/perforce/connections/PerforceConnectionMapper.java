package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface PerforceConnectionMapper {
  /**
   * @see VirtualFile#isInLocalFileSystem()
   * @param file - local file.
   * @return null in case of no connection exists or non-local VirtualFile
   */
  @Nullable
  P4Connection getConnection(@NotNull VirtualFile file);
  Map<VirtualFile, P4Connection> getAllConnections();
}
