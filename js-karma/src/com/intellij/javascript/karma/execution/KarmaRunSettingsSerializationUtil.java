package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class KarmaRunSettingsSerializationUtil {

  private static final String CONFIG_FILE = "config-file";
  private static final String KARMA_PACKAGE_DIR = "karma-package-dir";
  private static final String PASS_PARENT_ENV_VAR = "pass-parent-env-vars";
  private static final String BROWSERS = "browsers";

  private KarmaRunSettingsSerializationUtil() {}

  public static KarmaRunSettings readXml(@NotNull Element element) {
    KarmaRunSettings.Builder builder = new KarmaRunSettings.Builder();

    String configFilePath = getAttrValue(element, CONFIG_FILE);
    if (configFilePath == null) {
      configFilePath = JDOMExternalizerUtil.getFirstChildValueAttribute(element, CONFIG_FILE);
    }
    builder.setConfigPath(FileUtil.toSystemDependentName(StringUtil.notNullize(configFilePath)));

    String browsers = getAttrValue(element, BROWSERS);
    if (browsers == null) {
      browsers = JDOMExternalizerUtil.getFirstChildValueAttribute(element, BROWSERS);
    }
    if (browsers != null) {
      builder.setBrowsers(browsers);
    }
    String karmaPackageDir = JDOMExternalizerUtil.getFirstChildValueAttribute(element, KARMA_PACKAGE_DIR);
    builder.setKarmaPackageDir(FileUtil.toSystemDependentName(StringUtil.notNullize(karmaPackageDir)));

    Map<String, String> envVars = new LinkedHashMap<String, String>();
    EnvironmentVariablesComponent.readExternal(element, envVars);
    builder.setEnvVars(envVars);

    String passParentEnvVarsStr = getAttrValue(element, PASS_PARENT_ENV_VAR);
    if (passParentEnvVarsStr != null) {
      builder.setPassParentEnvVars(Boolean.parseBoolean(passParentEnvVarsStr));
    }

    return builder.build();
  }

  @Nullable
  private static String getAttrValue(@NotNull Element element, @NotNull String attrKey) {
    Attribute attribute = element.getAttribute(attrKey);
    return attribute != null ? attribute.getValue() : null;
  }

  public static void writeXml(@NotNull Element element, @NotNull KarmaRunSettings settings) {
    element.setAttribute(CONFIG_FILE, FileUtil.toSystemIndependentName(settings.getConfigPath()));
    if (StringUtil.isNotEmpty(settings.getBrowsers())) {
      element.setAttribute(BROWSERS, settings.getBrowsers());
    }
    if (StringUtil.isNotEmpty(settings.getKarmaPackageDir())) {
      JDOMExternalizerUtil.addElementWithValueAttribute(element, KARMA_PACKAGE_DIR, settings.getKarmaPackageDir());
    }
    EnvironmentVariablesComponent.writeExternal(element, settings.getEnvVars());
    if (settings.isPassParentEnvVars() != KarmaRunSettings.Builder.DEFAULT_PASS_PARENT_ENV_VARS) {
      element.setAttribute(PASS_PARENT_ENV_VAR, String.valueOf(settings.isPassParentEnvVars()));
    }
  }
}
