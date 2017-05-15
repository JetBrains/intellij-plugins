package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesData;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.NodePackage;
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
  private static final String NODE_INTERPRETER = "node-interpreter";

  private KarmaRunSettingsSerializationUtil() {}

  public static KarmaRunSettings readXml(@NotNull Element element) {
    KarmaRunSettings.Builder builder = new KarmaRunSettings.Builder();

    String configFilePath = JDOMExternalizerUtil.getFirstChildValueAttribute(element, CONFIG_FILE);
    if (configFilePath == null) {
      configFilePath = getAttrValue(element, CONFIG_FILE);
    }
    builder.setConfigPath(FileUtil.toSystemDependentName(StringUtil.notNullize(configFilePath)));

    String browsers = JDOMExternalizerUtil.getFirstChildValueAttribute(element, BROWSERS);
    if (browsers == null) {
      browsers = getAttrValue(element, BROWSERS);
    }
    if (browsers != null) {
      builder.setBrowsers(browsers);
    }
    String karmaPackageDir = JDOMExternalizerUtil.getFirstChildValueAttribute(element, KARMA_PACKAGE_DIR);
    if (karmaPackageDir != null) {
      builder.setKarmaPackage(new NodePackage(karmaPackageDir));
    }

    String interpreterRefName = JDOMExternalizerUtil.getFirstChildValueAttribute(element, NODE_INTERPRETER);
    builder.setInterpreterRef(interpreterRefName == null ? NodeJsInterpreterRef.createProjectRef()
                                                         : NodeJsInterpreterRef.create(interpreterRefName));

    EnvironmentVariablesData envData = EnvironmentVariablesData.readExternal(element);
    builder.setEnvData(envData);

    return builder.build();
  }

  @Nullable
  private static String getAttrValue(@NotNull Element element, @NotNull String attrKey) {
    Attribute attribute = element.getAttribute(attrKey);
    return attribute != null ? attribute.getValue() : null;
  }

  public static void writeXml(@NotNull Element element, @NotNull KarmaRunSettings settings) {
    JDOMExternalizerUtil.addElementWithValueAttribute(element,
                                                      CONFIG_FILE,
                                                      settings.getConfigSystemIndependentPath());
    if (StringUtil.isNotEmpty(settings.getBrowsers())) {
      JDOMExternalizerUtil.addElementWithValueAttribute(element, BROWSERS, settings.getBrowsers());
    }
    if (settings.getKarmaPackage() != null) {
      JDOMExternalizerUtil.addElementWithValueAttribute(element, KARMA_PACKAGE_DIR,
                                                        settings.getKarmaPackage().getSystemIndependentPath());
    }
    JDOMExternalizerUtil.addElementWithValueAttribute(element, NODE_INTERPRETER, settings.getInterpreterRef().getReferenceName());
    settings.getEnvData().writeExternal(element);
  }
}
