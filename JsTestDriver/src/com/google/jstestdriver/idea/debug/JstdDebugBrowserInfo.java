package com.google.jstestdriver.idea.debug;

import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.idea.server.JstdServerState;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
* @author Sergey Simonchik
*/
public class JstdDebugBrowserInfo<Connection> {

  private final JSDebugEngine<Connection> myDebugEngine;
  private final String myCapturedBrowserUrl;

  private JstdDebugBrowserInfo(@NotNull JSDebugEngine<Connection> debugEngine, @NotNull String capturedBrowserUrl) {
    myCapturedBrowserUrl = capturedBrowserUrl;
    myDebugEngine = debugEngine;
  }

  @NotNull
  public JSDebugEngine<Connection> getDebugEngine() {
    return myDebugEngine;
  }

  @NotNull
  public String getCapturedBrowserUrl() {
    return myCapturedBrowserUrl;
  }

  @Nullable
  public static <Connection> JstdDebugBrowserInfo<Connection> build() {
    JstdServerState jstdServerState = JstdServerState.getInstance();
    JSDebugEngine<?>[] engines = JSDebugEngine.getEngines();
    CapturedBrowsers browsers = jstdServerState.getCaptured();
    if (browsers == null) {
      return null;
    }
    for (SlaveBrowser slaveBrowser : browsers.getSlaveBrowsers()) {
      String browserName = slaveBrowser.getBrowserInfo().getName();
      for (JSDebugEngine<?> engine : engines) {
        if (engine.getId().equalsIgnoreCase(browserName)) {
          //noinspection unchecked
          JSDebugEngine<Connection> debugEngine = (JSDebugEngine<Connection>) engine;
          return new JstdDebugBrowserInfo<Connection>(debugEngine, slaveBrowser.getCaptureUrl());
        }
      }
    }
    return null;
  }

}
