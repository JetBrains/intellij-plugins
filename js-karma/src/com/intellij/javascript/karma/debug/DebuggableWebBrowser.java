package com.intellij.javascript.karma.debug;

import com.intellij.ide.browsers.WebBrowser;
import com.jetbrains.javascript.debugger.JSDebugEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DebuggableWebBrowser {
  private final JSDebugEngine myDebugEngine;
  private final WebBrowser myWebBrowser;

  public DebuggableWebBrowser(@NotNull JSDebugEngine debugEngine, @NotNull WebBrowser webBrowser) {
    myDebugEngine = debugEngine;
    myWebBrowser = webBrowser;
  }

  @NotNull
  public JSDebugEngine getDebugEngine() {
    return myDebugEngine;
  }

  @NotNull
  public WebBrowser getWebBrowser() {
    return myWebBrowser;
  }

  @Nullable
  public static DebuggableWebBrowser create(@NotNull WebBrowser browser) {
    JSDebugEngine debugEngine = JSDebugEngine.findByBrowser(browser);
    if (debugEngine != null) {
      return new DebuggableWebBrowser(debugEngine, browser);
    }
    return null;
  }
}
