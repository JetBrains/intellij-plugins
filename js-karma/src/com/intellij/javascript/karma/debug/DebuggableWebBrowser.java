package com.intellij.javascript.karma.debug;

import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.JavaScriptDebugEngine;
import com.intellij.openapi.util.registry.Registry;
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
    boolean withExtension = Registry.is("js.karma.debugWithExtension", false);
    JavaScriptDebugEngine debugEngine = JavaScriptDebugEngine.Companion.findByBrowser(browser, withExtension);
    return debugEngine != null ? new DebuggableWebBrowser(debugEngine, browser) : null;
  }
}
