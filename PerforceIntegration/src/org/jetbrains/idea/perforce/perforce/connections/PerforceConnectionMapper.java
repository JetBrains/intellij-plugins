package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author irengrig
 */
public interface PerforceConnectionMapper {
  @Nullable
  P4Connection getConnection(@NotNull final VirtualFile file);
  Map<VirtualFile, P4Connection> getAllConnections();
}
