package com.jetbrains.profiler;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: Maxim
 * Date: 21.09.2010
 * Time: 22:33:01
 */
public interface ProfilerSnapshotProvider {
  ExtensionPointName<ProfilerSnapshotProvider> ProfileSnapshotProvider_EP =
    ExtensionPointName.create("com.intellij.profiler.SnapshotProvider");

  boolean accepts(@NotNull VirtualFile file);

  @Nullable
  ProfileView createView(@NotNull VirtualFile file, @NotNull Project project);
}
