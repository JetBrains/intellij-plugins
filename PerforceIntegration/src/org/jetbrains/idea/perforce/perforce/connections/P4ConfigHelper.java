package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EnvironmentUtil;
import com.intellij.util.SystemProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.PerforcePhysicalConnectionParametersI;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class P4ConfigHelper {
  private static final List<String> ENV_CONFIGS = List.of(P4ConfigFields.P4PORT.getName(), P4ConfigFields.P4CLIENT.getName(),
                                                          P4ConfigFields.P4USER.getName(), P4ConfigFields.P4PASSWD.getName(),
                                                          P4ConfigFields.P4CONFIG.getName(), P4ConfigFields.P4IGNORE.getName());
  private final Project myProject;

  public P4ConfigHelper() {
    myProject = null;
  }

  public P4ConfigHelper(Project project) {
    myProject = project;
    reset();
  }

  public static P4ConfigHelper getConfigHelper(final Project project) {
    return project.getService(P4ConfigHelper.class);
  }

  public void reset() {
    PerforceSettings settings = PerforceSettings.getSettings(myProject);
    initializeP4SetVariables(myProject, settings.getPhysicalSettings());
  }

  private final Map<String, String> myDefaultParams = new HashMap<>();

  private void initializeP4SetVariables(Project project, PerforcePhysicalConnectionParametersI physicalParameters) {
    myDefaultParams.clear();

    P4ParamsCalculator calculator = new P4ParamsCalculator(project);
    P4ConnectionParameters defaultParams = new P4ConnectionParameters();
    calculator.runSetOnFile(physicalParameters, defaultParams, SystemProperties.getUserHome());

    for (String envVar : ENV_CONFIGS) {
      String value = EnvironmentUtil.getValue(envVar);
      tryToPutVariable(envVar, value);
    }

    tryToPutVariable(P4ConfigFields.P4PORT.getName(), defaultParams.getServer());
    tryToPutVariable(P4ConfigFields.P4USER.getName(), defaultParams.getUser());
    tryToPutVariable(P4ConfigFields.P4CLIENT.getName(), defaultParams.getClient());
    tryToPutVariable(P4ConfigFields.P4PASSWD.getName(), defaultParams.getPassword());
    tryToPutVariable(P4ConfigFields.P4CONFIG.getName(), defaultParams.getConfigFileName());
    tryToPutVariable(P4ConfigFields.P4IGNORE.getName(), defaultParams.getIgnoreFileName());
    tryToPutVariable(P4ConfigFields.P4CHARSET.getName(), defaultParams.getCharset());
  }

  private void tryToPutVariable(String variable, @Nullable String value) {
    if (value != null) {
      myDefaultParams.put(variable, value);
    }
  }

  public boolean hasP4ConfigSetting() {
    return myDefaultParams.get(P4ConfigFields.P4CONFIG.getName()) != null;
  }

  public String getUnsetP4EnvironmentVars() {
    return ENV_CONFIGS.stream()
      .filter(env -> myDefaultParams.get(env) == null)
      .collect(Collectors.joining(","));
  }

  public boolean hasP4IgnoreSetting() {
    return myDefaultParams.get(P4ConfigFields.P4IGNORE.getName()) != null;
  }

  @Nullable
  public String getP4Config() {
    return myDefaultParams.get(P4ConfigFields.P4CONFIG.getName());
  }

  public void fillDefaultValues(P4ConnectionParameters parameters) {
    if (parameters.getServer() == null)
      parameters.setServer(myDefaultParams.get(P4ConfigFields.P4PORT.getName()));
    if (parameters.getUser() == null)
      parameters.setUser(myDefaultParams.get(P4ConfigFields.P4USER.getName()));
    if (parameters.getClient() == null)
      parameters.setClient(myDefaultParams.get(P4ConfigFields.P4CLIENT.getName()));
    if (parameters.getPassword() == null)
      parameters.setPassword(myDefaultParams.get(P4ConfigFields.P4PASSWD.getName()));
  }

  @Nullable
  public static String getP4IgnoreFileNameFromEnv() { return EnvironmentUtil.getValue(P4ConfigFields.P4IGNORE.getName()); }

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
