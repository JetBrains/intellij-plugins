package com.intellij.javascript.karma.execution;

import com.intellij.openapi.util.JDOMExternalizer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class KarmaRunSettingsSerializationUtil {

  private static final String CONFIG_PATH = "config_file";

  private KarmaRunSettingsSerializationUtil() {}

  public static KarmaRunSettings readFromXml(@NotNull Element element) {
    String configPath = StringUtil.notNullize(JDOMExternalizer.readString(element, CONFIG_PATH));
    configPath = FileUtil.toSystemDependentName(configPath);
    return new KarmaRunSettings.Builder().setConfigPath(configPath).build();
  }

  public static void writeToXml(@NotNull Element element, @NotNull KarmaRunSettings settings) {
    String configPath = FileUtil.toSystemIndependentName(settings.getConfigPath());
    JDOMExternalizer.write(element, CONFIG_PATH, configPath);
  }
}
