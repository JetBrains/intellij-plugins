package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.util.JDOMExternalizer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Sergey Simonchik
 */
public class KarmaRunSettingsSerializationUtil {

  private static final String OLD_CONFIG_PATH = "config_file";
  private static final String CONFIG_FILE = "config-file";
  private static final String PASS_PARENT_ENV_VAR = "pass-parent-env-vars";

  private KarmaRunSettingsSerializationUtil() {}

  public static KarmaRunSettings readFromXml(@NotNull Element element) {
    KarmaRunSettings.Builder builder = new KarmaRunSettings.Builder();

    String configPath = getAttrValue(element, CONFIG_FILE);
    if (configPath == null) {
      configPath = StringUtil.notNullize(JDOMExternalizer.readString(element, OLD_CONFIG_PATH));
    }
    builder.setConfigPath(FileUtil.toSystemDependentName(configPath));

    Map<String, String> envVars = ContainerUtil.newHashMap();
    EnvironmentVariablesComponent.readExternal(element, envVars);
    builder.setEnvVars(envVars);

    String passParentEnvVarsStr = getAttrValue(element, PASS_PARENT_ENV_VAR);
    if (passParentEnvVarsStr != null) {
      builder.setPassParentEnvVars(Boolean.parseBoolean(passParentEnvVarsStr));
    }

    return builder.build();
  }

  @Nullable
  private static String getAttrValue(Element element, String attrKey) {
    Attribute attribute = element.getAttribute(attrKey);
    return attribute != null ? attribute.getValue() : null;
  }

  public static void writeToXml(@NotNull Element element, @NotNull KarmaRunSettings settings) {
    element.setAttribute(CONFIG_FILE, FileUtil.toSystemIndependentName(settings.getConfigPath()));
    Map<String, String> envVars = getEnvVarsForWriting(settings.getEnvVars());
    EnvironmentVariablesComponent.writeExternal(element, envVars);
    if (settings.isPassParentEnvVars() != KarmaRunSettings.Builder.DEFAULT_PASS_PARENT_ENV_VARS) {
      element.setAttribute(PASS_PARENT_ENV_VAR, String.valueOf(settings.isPassParentEnvVars()));
    }
  }

  @NotNull
  private static Map<String, String> getEnvVarsForWriting(@NotNull Map<String, String> envVars) {
    if (envVars.size() <= 1) {
      return envVars;
    }
    return new TreeMap<String, String>(envVars);
  }
}
