package com.google.jstestdriver.idea.execution.tree;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class ConfigNode extends AbstractSuiteNode<BrowserNode> {
  private final File myConfigFile;

  public ConfigNode(@NotNull String name, @NotNull File configFile, @NotNull RootNode parent) {
    super(name, parent);
    myConfigFile = configFile;
  }

  @Override
  public String getProtocolId() {
    return "config";
  }

  @Override
  public String getLocationPath() {
    return myConfigFile.getAbsolutePath();
  }
}
