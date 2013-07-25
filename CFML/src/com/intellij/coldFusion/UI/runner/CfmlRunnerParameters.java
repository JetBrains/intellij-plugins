package com.intellij.coldFusion.UI.runner;

import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CfmlRunnerParameters implements Cloneable {
  private static final String DEFAULT = "DEFAULT";

  private String myUrl = "";
  @Nullable private BrowsersConfiguration.BrowserFamily myNonDefaultBrowser;

  @Attribute("web_path")
  public String getUrl() {
    return myUrl;
  }

  public void setUrl(@NotNull String url) {
    myUrl = url;
  }

  @Transient
  @Nullable
  public BrowsersConfiguration.BrowserFamily getNonDefaultBrowser() {
    return myNonDefaultBrowser;
  }

  public void setNonDefaultBrowser(@Nullable BrowsersConfiguration.BrowserFamily nonDefaultBrowser) {
    myNonDefaultBrowser = nonDefaultBrowser;
  }

  @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException"})
  @Override
  protected CfmlRunnerParameters clone() {
    try {
      return (CfmlRunnerParameters)super.clone();
    }
    catch (CloneNotSupportedException e) {
      //noinspection ConstantConditions
      return null;
    }
  }
}
