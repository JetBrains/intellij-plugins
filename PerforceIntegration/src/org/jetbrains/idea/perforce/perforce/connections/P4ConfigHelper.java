package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class P4ConfigHelper {
  private final Map<File, File> myAlreadyFoundConfigs = new HashMap<>();

  @Nullable
  public File findDirWithP4ConfigFile(@NotNull final VirtualFile parent, @NotNull final String p4ConfigFileName) {
    File current = VfsUtilCore.virtualToIoFile(parent);

    final List<File> paths = new ArrayList<>();
    while (current != null) {
      final File calculated = myAlreadyFoundConfigs.get(current);
      if (calculated != null) {
        return cacheForAllChildren(paths, calculated);
      }

      File candidate = new File(current, p4ConfigFileName);
      if (candidate.exists() && !candidate.isDirectory()) {
        myAlreadyFoundConfigs.put(current, current);
        return cacheForAllChildren(paths, current);
      }

      paths.add(current);
      current = current.getParentFile();
    }
    return null;
  }

  private File cacheForAllChildren(List<File> paths, File calculated) {
    for (File path : paths) {
      myAlreadyFoundConfigs.put(path, calculated);
    }
    return calculated;
  }
}
