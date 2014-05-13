package com.google.jstestdriver.idea.debug;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.server.JstdBrowserInfo;
import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerRegistry;
import com.google.jstestdriver.idea.server.JstdServerSettings;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import com.intellij.openapi.util.Pair;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
* @author Sergey Simonchik
*/
public class JstdDebugBrowserInfo {

  private final Pair<JSDebugEngine, WebBrowser> myDebugEngine;
  private final JstdServerSettings myServerSettings;
  private final JstdBrowserInfo myBrowserInfo;

  private JstdDebugBrowserInfo(@NotNull Pair<JSDebugEngine, WebBrowser> debugEngine,
                               @NotNull JstdServerSettings serverSettings,
                               @NotNull JstdBrowserInfo browserInfo) {
    myDebugEngine = debugEngine;
    myServerSettings = serverSettings;
    myBrowserInfo = browserInfo;
  }

  @NotNull
  public JSDebugEngine getDebugEngine() {
    return myDebugEngine.first;
  }

  @NotNull
  public WebBrowser getBrowser() {
    return myDebugEngine.second;
  }

  @NotNull
  public JstdServerSettings getServerSettings() {
    return myServerSettings;
  }

  @NotNull
  public String getPath() {
    return "/slave/id/" + myBrowserInfo.getId()
           + "/page/CONSOLE/mode/quirks/timeout/" + myServerSettings.getBrowserTimeoutMillis()
           + "/upload_size/50/rt/CLIENT";
  }

  @Nullable
  public static JstdDebugBrowserInfo build(@NotNull JstdRunSettings runSettings) {
    JstdServer server = JstdServerRegistry.getInstance().getServer();
    if (server == null || !server.isProcessRunning()) {
      return null;
    }
    Collection<JstdBrowserInfo> capturedBrowsers = server.getCapturedBrowsers();

    List<JstdDebugBrowserInfo> debugBrowserInfos = new SmartList<JstdDebugBrowserInfo>();
    for (JstdBrowserInfo browserInfo : capturedBrowsers) {
      Pair<JSDebugEngine, WebBrowser> engine = JSDebugEngine.findByBrowserName(browserInfo.getName());
      if (engine != null) {
        debugBrowserInfos.add(new JstdDebugBrowserInfo(engine, server.getSettings(), browserInfo));
      }
    }
    if (debugBrowserInfos.size() == 1) {
      return debugBrowserInfos.get(0);
    }
    if (debugBrowserInfos.size() > 1) {
      WebBrowser preferredBrowser = runSettings.getPreferredDebugBrowser();
      for (JstdDebugBrowserInfo info : debugBrowserInfos) {
        if (preferredBrowser.equals(info.getBrowser())) {
          return info;
        }
      }
    }
    return null;
  }
}
