package com.intellij.javascript.karma.debug;

import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.JavaScriptDebugEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DebuggableWebBrowser {
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
    JavaScriptDebugEngine debugEngine = JavaScriptDebugEngine.Companion.findByBrowser(browser, false);
    return debugEngine != null ? new DebuggableWebBrowser(debugEngine, browser) : null;
  }
}
