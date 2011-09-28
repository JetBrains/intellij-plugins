package com.intellij.openapi.application;

import java.io.File;
import java.text.MessageFormat;

/**
 * User: ksafonov
 */
public class FlexImportOldConfigsPanel extends ImportOldConfigsPanel {

  public FlexImportOldConfigsPanel(final File guessedOldConfig) {
    super(guessedOldConfig);
  }

  @Override
  protected String getProductName(boolean full) {
    return "IntelliJ IDEA";
  }

  @Override
  protected String getTitleLabel(String productName) {
    return
      "<html>You can import your settings from existing installation of IntelliJ IDEA.<br>This is recommended if you plan to open your IntelliJ IDEA projects in " +
      ApplicationNamesInfo.getInstance().getProductName() + ".</html>";
  }

  @Override
  protected String getDoNotImportLabel(String productName) {
    return "I do not have an installation of IntelliJ IDEA or I do not want to import my settings";
  }

  @Override
  protected String getAutoImportLabel(File guessedOldConfig) {
    return MessageFormat.format("I want to import my settings from existing installation ({0})", guessedOldConfig);
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
}
