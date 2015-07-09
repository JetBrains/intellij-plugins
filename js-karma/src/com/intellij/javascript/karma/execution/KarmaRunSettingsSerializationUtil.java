package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.execution.configuration.EnvironmentVariablesData;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KarmaRunSettingsSerializationUtil {

  private static final String CONFIG_FILE = "config-file";
  private static final String KARMA_PACKAGE_DIR = "karma-package-dir";
  private static final String BROWSERS = "browsers";
  private static final String OLD_PASS_PARENT_ENV_VAR = "pass-parent-env-vars"; // TODO to be removed in IDEA 16

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
    if (karmaPackageDir != null) {
      builder.setKarmaPackageDir(FileUtil.toSystemDependentName(karmaPackageDir));
    }

    EnvironmentVariablesData envData = EnvironmentVariablesData.readExternal(element);
    String passParentEnvVarsStr = getAttrValue(element, OLD_PASS_PARENT_ENV_VAR);
    if (passParentEnvVarsStr != null) {
      envData = EnvironmentVariablesData.create(envData.getEnvs(), Boolean.parseBoolean(passParentEnvVarsStr));
    }
    builder.setEnvData(envData);

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
    if (settings.getKarmaPackageDir() != null) {
      String value = FileUtil.toSystemIndependentName(settings.getKarmaPackageDir());
      JDOMExternalizerUtil.addElementWithValueAttribute(element, KARMA_PACKAGE_DIR, value);
    }
    EnvironmentVariablesComponent.writeExternal(element, settings.getEnvData().getEnvs());
    if (!settings.getEnvData().isPassParentEnvs()) {
      element.setAttribute(OLD_PASS_PARENT_ENV_VAR, String.valueOf(settings.getEnvData().isPassParentEnvs()));
    }
  }
}
