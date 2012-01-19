package com.jetbrains.actionscript.profiler;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.actionscript.profiler.file.CpuSnapshotFileType;
import com.jetbrains.actionscript.profiler.file.LiveObjectsFileType;
import com.jetbrains.actionscript.profiler.ui.CPUSnapshotView;
import com.jetbrains.actionscript.profiler.ui.LiveObjectsView;
import com.jetbrains.profiler.ProfileView;
import com.jetbrains.profiler.ProfilerSnapshotProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: Maxim
 * Date: 21.09.2010
 * Time: 22:38:55
 */
public class ActionScriptProfileProvider implements ProfilerSnapshotProvider {

  public boolean accepts(@NotNull VirtualFile file) {
    return file.getFileType() instanceof CpuSnapshotFileType || file.getFileType() instanceof LiveObjectsFileType;
  }

  @Nullable
  public ProfileView createView(@NotNull VirtualFile file, @NotNull Project project) {
    if (file.getFileType() instanceof CpuSnapshotFileType) {
      return new CPUSnapshotView(file, project);
    }
    if (file.getFileType() instanceof LiveObjectsFileType) {
      return new LiveObjectsView(file, project);
    }
    return null;
  }
}
