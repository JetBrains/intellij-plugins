package com.intellij.openapi.application;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.SystemProperties;
import com.intellij.util.ThreeState;

import java.io.File;
import java.text.MessageFormat;

/**
 * User: ksafonov
 */

// see com.intellij.openapi.application.ConfigImportHelper.getConfigImportSettings
@SuppressWarnings("UnusedDeclaration")
public class FlexConfigImportSettings extends ConfigImportSettings {

  @Override
  protected String getProductName(ThreeState full) {
    return full == ThreeState.NO ? "IDEA" : "IntelliJ IDEA";
  }

  @Override
  protected String getTitleLabel(String productName) {
    return
      "<html>You can import your settings from existing installation of IntelliJ IDEA.<br>This is recommended if you plan to open your IntelliJ IDEA projects in " +
      ApplicationNamesInfo.getInstance().getProductName() +
      ".</html>";
  }

  @Override
  protected String getDoNotImportLabel(String productName) {
    return "I do not have an installation of IntelliJ IDEA or I do not want to import my settings";
  }

  @Override
  protected String getAutoImportLabel(File guessedOldConfig) {
    String shortPath = guessedOldConfig.getAbsolutePath().replace(SystemProperties.getUserHome(), "~");
    return MessageFormat.format("I want to import my settings from existing installation (config folder: {0})", shortPath);
  }

  @Override
  protected String getHomeLabel(String productName) {
    return "Specify config folder or installation home of IntelliJ IDEA:";
  }

  @Override
  protected String getEmptyHomeErrorText(String productWithVendor) {
    return "Please select existing IntelliJ IDEA config folder or installation home";
  }

  @Override
  protected String getCurrentHomeErrorText(String productWithVendor) {
    return MessageFormat.format("You have selected current {0} installation home.\\nPlease select existing IntelliJ IDEA installation home",
                                productWithVendor);
  }

  @Override
  protected String getInvalidHomeErrorText(String productWithVendor, String instHome) {
    return MessageFormat.format("{0} does not appear to be IntelliJ IDEA config folder or installation home", instHome);
  }

  @Override
  protected String getInaccessibleHomeErrorText(String instHome) {
    return super.getInaccessibleHomeErrorText(instHome);    //To change body of overridden methods use File | Settings | File Templates.
  }

  @Override
  public void importFinished(String newConfigPath) {
    File disabledPluginsFile = new File(newConfigPath, PluginManager.DISABLED_PLUGINS_FILENAME);
    if (disabledPluginsFile.exists()) {
      FileUtil.delete(disabledPluginsFile);
    }
  }

  @Override
  public String getCustomPathsSelector() {
    return ".IntelliJIdea10";
  }

}
