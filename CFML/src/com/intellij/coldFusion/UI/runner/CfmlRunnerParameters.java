// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.runner;

import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CfmlRunnerParameters implements Cloneable {
  private String myUrl = "";
  private String myCustomBrowserId = "";

  @Attribute("web_path")
  public @NlsSafe String getUrl() {
    return myUrl;
  }

  public void setUrl(@NotNull String url) {
    myUrl = url;
  }

  @Attribute("custom_browser")
  public String getCustomBrowserId() {
    return myCustomBrowserId;
  }

  public void setCustomBrowserId(@NotNull String customBrowserId) {
    myCustomBrowserId = customBrowserId;
  }

  @Transient
  public @Nullable WebBrowser getCustomBrowser() {
    String customBrowserId = getCustomBrowserId();
    if (!customBrowserId.isEmpty()) {
      return WebBrowserManager.getInstance().findBrowserById(customBrowserId);
    }
    return null;
  }

  public void setCustomBrowser(@Nullable WebBrowser customBrowser) {
    setCustomBrowserId(customBrowser != null ? customBrowser.getId().toString() : "");
  }

  @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException"})
  @Override
  protected CfmlRunnerParameters clone() {
    try {
      return (CfmlRunnerParameters)super.clone();
    }
    catch (CloneNotSupportedException e) {
      return null;
    }
  }
}
