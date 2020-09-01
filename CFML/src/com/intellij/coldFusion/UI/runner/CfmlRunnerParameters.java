/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  @Nullable
  public WebBrowser getCustomBrowser() {
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
