package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.P4File;

import java.io.File;
import java.util.Map;

public interface PerforceConnectionManagerI {
  @Nullable
  PerforceMultipleConnections getMultipleConnectionObject();
  @NotNull
  Map<VirtualFile, P4Connection> getAllConnections();
  @Nullable
  P4Connection getConnectionForFile(@NotNull File file);
  @Nullable
  P4Connection getConnectionForFile(@NotNull P4File file);
  @Nullable
  P4Connection getConnectionForFile(@NotNull VirtualFile file);
  boolean isSingletonConnectionUsed();
  void updateConnections();

  boolean isUnderProjectConnections(@NotNull File file);
  boolean isInitialized();
}
