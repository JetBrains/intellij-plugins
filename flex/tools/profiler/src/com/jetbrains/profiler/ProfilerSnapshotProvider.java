package com.jetbrains.profiler;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ProfilerSnapshotProvider {
  ExtensionPointName<ProfilerSnapshotProvider> ProfileSnapshotProvider_EP =
    ExtensionPointName.create("com.intellij.profiler.SnapshotProvider");

  boolean accepts(@NotNull VirtualFile file);

  @Nullable
  ProfileView createView(@NotNull VirtualFile file, @NotNull Project project);
}
