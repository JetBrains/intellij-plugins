package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EnvironmentUtil;
import com.intellij.util.SystemProperties;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.PerforcePhysicalConnectionParametersI;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class P4EnvHelper {
  private static final List<String> ENV_CONFIGS = List.of(P4ConfigFields.P4PORT.getName(), P4ConfigFields.P4CLIENT.getName(),
                                                          P4ConfigFields.P4USER.getName(), P4ConfigFields.P4PASSWD.getName(),
                                                          P4ConfigFields.P4CONFIG.getName(), P4ConfigFields.P4IGNORE.getName());
  private final Project myProject;

  public P4EnvHelper(Project project) {
    myProject = project;
    reset();
  }

  public P4ConnectionParameters getDefaultParams() {
    return myDefaultParams;
  }

  public static P4EnvHelper getConfigHelper(final Project project) {
    return project.getService(P4EnvHelper.class);
  }

  public void reset() {
    PerforceSettings settings = PerforceSettings.getSettings(myProject);
    initializeP4SetVariables(myProject, settings.getPhysicalSettings(false));
    settings.setEnvP4IgnoreVar(getP4Ignore());
  }

  private final Map<String, String> myDefaultParamsMap = new HashMap<>();
  private P4ConnectionParameters myDefaultParams;

  private void initializeP4SetVariables(Project project, PerforcePhysicalConnectionParametersI physicalParameters) {
    final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
    final PerforceVcs vcs = PerforceVcs.getInstance(myProject);
    final List<VirtualFile> detailedVcsMappings = Registry.is("p4.new.project.mappings.handling")
                                                  ? Arrays.asList(vcsManager.getRootsUnderVcs(vcs))
                                                  : vcsManager.getDetailedVcsMappings(vcs);
    P4ParamsCalculator calculator = new P4ParamsCalculator(project);

    synchronized (this) {
      myDefaultParamsMap.clear();
      myDefaultParams = new P4ConnectionParameters();
      P4ConnectionParameters params = calculator.runSetOnFile(physicalParameters, myDefaultParams, SystemProperties.getUserHome());
      takeProblemsIntoDefaultParams(params, myDefaultParams);
      for (VirtualFile vcsMapping : detailedVcsMappings) {
        P4ConnectionParameters mappingParams = calculator.runSetOnFile(physicalParameters, myDefaultParams, vcsMapping.getPath());
        takeProblemsIntoDefaultParams(mappingParams, myDefaultParams);
        if (myDefaultParams.allFieldsDefined())
          break;
      }

      for (String envVar : ENV_CONFIGS) {
        String value = EnvironmentUtil.getValue(envVar);
        tryToPutVariable(envVar, value);
      }

      tryToPutVariable(P4ConfigFields.P4PORT.getName(), myDefaultParams.getServer());
      tryToPutVariable(P4ConfigFields.P4USER.getName(), myDefaultParams.getUser());
      tryToPutVariable(P4ConfigFields.P4CLIENT.getName(), myDefaultParams.getClient());
      tryToPutVariable(P4ConfigFields.P4PASSWD.getName(), myDefaultParams.getPassword());
      tryToPutVariable(P4ConfigFields.P4CONFIG.getName(), myDefaultParams.getConfigFileName());
      tryToPutVariable(P4ConfigFields.P4IGNORE.getName(), myDefaultParams.getIgnoreFileName());
      tryToPutVariable(P4ConfigFields.P4CHARSET.getName(), myDefaultParams.getCharset());
    }
  }

  private void tryToPutVariable(String variable, @Nullable String value) {
    if (value != null) {
      myDefaultParamsMap.put(variable, value);
    }
  }

  private static void takeProblemsIntoDefaultParams(P4ConnectionParameters params, P4ConnectionParameters defaultParameters) {
    if (params.hasProblems()) {
      if (params.getException() != null) {
        defaultParameters.setException(params.getException());
      }
      if (! params.getWarnings().isEmpty()) {
        for (String warning : params.getWarnings()) {
          defaultParameters.addWarning(warning);
        }
      }
    }
  }

  public synchronized String getUnsetP4EnvironmentVars() {
    return ENV_CONFIGS.stream()
      .filter(env -> myDefaultParamsMap.get(env) == null)
      .collect(Collectors.joining(","));
  }

  public synchronized boolean hasP4ConfigSetting() {
    return myDefaultParamsMap.get(P4ConfigFields.P4CONFIG.getName()) != null;
  }

  public synchronized boolean hasP4IgnoreSetting() {
    return myDefaultParamsMap.get(P4ConfigFields.P4IGNORE.getName()) != null;
  }

  @Nullable
  public synchronized String getP4Config() {
    return myDefaultParamsMap.get(P4ConfigFields.P4CONFIG.getName());
  }

  @Nullable
  public synchronized String getP4Ignore() {
    return myDefaultParamsMap.get(P4ConfigFields.P4IGNORE.getName());
  }

  public synchronized void fillDefaultValues(P4ConnectionParameters parameters) {
    if (parameters.getServer() == null)
      parameters.setServer(myDefaultParamsMap.get(P4ConfigFields.P4PORT.getName()));
    if (parameters.getUser() == null)
      parameters.setUser(myDefaultParamsMap.get(P4ConfigFields.P4USER.getName()));
    if (parameters.getClient() == null)
      parameters.setClient(myDefaultParamsMap.get(P4ConfigFields.P4CLIENT.getName()));
    if (parameters.getPassword() == null)
      parameters.setPassword(myDefaultParamsMap.get(P4ConfigFields.P4PASSWD.getName()));
    if (parameters.getIgnoreFileName() == null)
      parameters.setIgnoreFileName(myDefaultParams.getIgnoreFileName());
  }

  @Nullable
  public static String getP4IgnoreFileNameFromEnv() {
    String testValue = AbstractP4Connection.getTestEnvironment().get(P4ConfigFields.P4IGNORE.getName());
    if (testValue != null) return testValue;

    return EnvironmentUtil.getValue(P4ConfigFields.P4IGNORE.getName());
  }

  public static boolean hasP4ConfigSettingInEnvironment() {
    return EnvironmentUtil.getValue(P4ConfigFields.P4CONFIG.getName()) != null;
  }
}
