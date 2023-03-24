package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.project.BaseProjectDirectories;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EnvironmentUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.PerforcePhysicalConnectionParametersI;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import java.io.File;
import java.nio.file.Path;
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
  private final Project myProject;

  public P4ConfigHelper() {
    myProject = null;
  }

  public P4ConfigHelper(Project project) {
    myProject = project;
    PerforceSettings settings = PerforceSettings.getSettings(project);
    initializeP4SetVariables(myProject, settings.getPhysicalSettings());
  }

  public static P4ConfigHelper getConfigHelper(final Project project) {
    return project.getService(P4ConfigHelper.class);
  }

  private final Map<Path, P4ConnectionParameters> myRootParameters = new HashMap<>();
  private String myP4ConfigName = getP4ConfigFileNameFromEnv();

  public void initializeP4SetVariables(Project project, PerforcePhysicalConnectionParametersI physicalParameters) {
    myRootParameters.clear();
    P4ConnectionCalculator calculator = new P4ConnectionCalculator(project);

    // P4 global variables might be set not only through env vars but also with p4 set command (in p4enviro file).
    for (VirtualFile dir : BaseProjectDirectories.getBaseDirectories(project)) {
      P4ConnectionParameters params = calculator.runSetOnFile(physicalParameters, new P4ConnectionParameters(), dir.getPath());

      // We don't expect P4Config variable to be changed inside P4Config files
      // (it changes what you will see in p4 set output but does nothing)
      // So if we get P4Config value from p4 set, while env var is not set, we will pretend that it comes from P4Enviro file
      if (myP4ConfigName == null) {
        myP4ConfigName = params.getConfigFileName();
      }

      myRootParameters.put(dir.toNioPath(), params);
    }
  }

  public static boolean hasP4ConfigSettingInEnvironment() {
    return EnvironmentUtil.getValue(P4_CONFIG) != null;
  }

  // todo: fix for P4Enviro
  public static String getUnsetP4EnvironmentConfig() {
    return ENV_CONFIGS.stream()
      .filter(env -> EnvironmentUtil.getValue(env) == null)
      .collect(Collectors.joining(","));
  }

  public static boolean hasP4IgnoreSettingInEnvironment() {
    return EnvironmentUtil.getValue(P4_IGNORE) != null;
  }

  @Nullable
  public static String getP4ConfigFileNameFromEnv() {
    return EnvironmentUtil.getValue(P4_CONFIG);
  }

  // todo: merge with the method above
  @Nullable
  public String getP4Config() {
    return myP4ConfigName;
  }

  @Nullable
  public static String getP4IgnoreFileNameFromEnv() { return EnvironmentUtil.getValue(P4_IGNORE); }

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
