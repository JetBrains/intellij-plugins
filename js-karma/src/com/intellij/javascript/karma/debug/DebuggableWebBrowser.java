// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.debug;

import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.JavaScriptDebugEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DebuggableWebBrowser {
  private final JavaScriptDebugEngine myDebugEngine;
  private final WebBrowser myWebBrowser;

  private DebuggableWebBrowser(@NotNull JavaScriptDebugEngine debugEngine, @NotNull WebBrowser webBrowser) {
    myDebugEngine = debugEngine;
    myWebBrowser = webBrowser;
  }

  public @NotNull JavaScriptDebugEngine getDebugEngine() {
    return myDebugEngine;
  }

  public @NotNull WebBrowser getWebBrowser() {
    return myWebBrowser;
  }

  public static @Nullable DebuggableWebBrowser create(@NotNull WebBrowser browser) {
    JavaScriptDebugEngine debugEngine = JavaScriptDebugEngine.Companion.findByBrowser(browser);
    return debugEngine != null ? new DebuggableWebBrowser(debugEngine, browser) : null;
  }
}
