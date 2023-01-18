package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EnvironmentUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class P4ConfigHelper {
  @NonNls public static final String P4_CONFIG = "P4CONFIG";
  @NonNls public static final String P4_IGNORE = "P4IGNORE";
  private static final List<String> ENV_CONFIGS = List.of(P4ConfigFields.P4PORT.getName(), P4ConfigFields.P4CLIENT.getName(),
                                                          P4ConfigFields.P4USER.getName(), P4ConfigFields.P4PASSWD.getName(),
                                                          P4ConfigFields.P4CONFIG.getName());

  public static boolean hasP4ConfigSettingInEnvironment() {
    return EnvironmentUtil.getValue(P4_CONFIG) != null;
  }

  public static String getUnsetP4EnvironmentConfig() {
    return ENV_CONFIGS.stream()
      .filter(env -> EnvironmentUtil.getValue(env) == null)
      .collect(Collectors.joining(","));
  }

  public static boolean hasP4IgnoreSettingInEnvironment() {
    return EnvironmentUtil.getValue(P4_IGNORE) != null;
  }

  @Nullable
  public static String getP4ConfigFileName() {
    return EnvironmentUtil.getValue(P4_CONFIG);
  }

  @Nullable
  public static String getP4IgnoreVariable() { return EnvironmentUtil.getValue(P4_IGNORE); }

  private final Map<File, File> myAlreadyFoundConfigs = new HashMap<>();

  @Nullable
  public static String getP4IgnoreFileName() {
    String testValue = AbstractP4Connection.getTestEnvironment().get(P4ConfigFields.P4IGNORE.getName());
    if (testValue != null) return testValue;

    return EnvironmentUtil.getValue(P4ConfigFields.P4IGNORE.getName());
  }

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
