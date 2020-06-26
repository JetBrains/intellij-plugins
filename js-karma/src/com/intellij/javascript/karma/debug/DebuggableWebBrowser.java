// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.debug;

import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.JavaScriptDebugEngine;
import com.intellij.javascript.debugger.execution.ILiveEditOptions;
import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DebuggableWebBrowser {
  private final JavaScriptDebugEngine myDebugEngine;
  private final WebBrowser myWebBrowser;

  private DebuggableWebBrowser(@NotNull JavaScriptDebugEngine debugEngine, @NotNull WebBrowser webBrowser) {
    myDebugEngine = debugEngine;
    myWebBrowser = webBrowser;
  }

  @NotNull
  public JavaScriptDebugEngine getDebugEngine() {
    return myDebugEngine;
  }

  @NotNull
  public WebBrowser getWebBrowser() {
    return myWebBrowser;
  }

  @Nullable
  public static DebuggableWebBrowser create(@NotNull WebBrowser browser) {
    ILiveEditOptions liveEditOptions = ServiceManager.getService(ILiveEditOptions.class);
    boolean useExtension = liveEditOptions != null && liveEditOptions.isUseJBChromeExtension();
    JavaScriptDebugEngine debugEngine = JavaScriptDebugEngine.Companion.findByBrowser(browser, useExtension);
    return debugEngine != null ? new DebuggableWebBrowser(debugEngine, browser) : null;
  }
}
