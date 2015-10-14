package com.google.jstestdriver.idea.rt.execution.tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class ConfigNode extends AbstractSuiteNode<BrowserNode> {
  private final File myConfigFile;
  private String myAbsoluteBasePath;

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

  public void setBasePath(@NotNull String absoluteBasePath) {
    myAbsoluteBasePath = absoluteBasePath;
  }

  @Nullable
  public String getAbsoluteBasePath() {
    return myAbsoluteBasePath;
  }
}
