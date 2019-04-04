package com.google.jstestdriver.idea.debug;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.server.JstdBrowserInfo;
import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerSettings;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.JavaScriptDebugEngine;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.util.SmartList;
import com.intellij.util.TimeoutUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class JstdDebugBrowserInfo {
  private static final Logger LOG = Logger.getInstance(JstdDebugBrowserInfo.class);

  private final JavaScriptDebugEngine myDebugEngine;
  private final WebBrowser myWebBrowser;
  private final JstdServerSettings myServerSettings;
  private final JstdBrowserInfo myBrowserInfo;

  private JstdDebugBrowserInfo(@NotNull Pair<JavaScriptDebugEngine, WebBrowser> pair,
                               @NotNull JstdServerSettings serverSettings,
                               @NotNull JstdBrowserInfo browserInfo) {
    myDebugEngine = pair.first;
    myWebBrowser = pair.second;
    myServerSettings = serverSettings;
    myBrowserInfo = browserInfo;
  }

  @NotNull
  public JavaScriptDebugEngine getDebugEngine() {
    return myDebugEngine;
  }

  @NotNull
  public WebBrowser getBrowser() {
    return myWebBrowser;
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

  /**
   * Posts 'heartbeat' event to the server. Usually, a captured browser posts this event, but Chrome is a special case:
   *   When execution suspended on a breakpoint, it can't perform any background activity.
   * Emulating 'heartbeat' event keeps alive the debug session.
   *
   * @param testRunnerProcessHandler
   */
  public void fixIfChrome(@NotNull final ProcessHandler testRunnerProcessHandler) {
    if (!BrowserFamily.CHROME.equals(myWebBrowser.getFamily())) {
      return;
    }
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      HttpClient client = new HttpClient();
      while (!testRunnerProcessHandler.isProcessTerminated()) {
        String url = "http://127.0.0.1:" + myServerSettings.getPort() + "/heartbeat";
        PostMethod method = new PostMethod(url);
        method.addParameter("id", myBrowserInfo.getId());
        try {
          int responseCode = client.executeMethod(method);
          if (responseCode != 200) {
            LOG.warn(url + ": response code: " + responseCode);
          }
        }
        catch (IOException e) {
          LOG.warn("Cannot request " + url, e);
        }
        TimeoutUtil.sleep(5000);
      }
      client.getHttpConnectionManager().closeIdleConnections(0);
    });
  }

  @Nullable
  public static JstdDebugBrowserInfo build(@NotNull JstdServer server, @NotNull JstdRunSettings runSettings) {
    Collection<JstdBrowserInfo> capturedBrowsers = server.getCapturedBrowsers();
    List<JstdDebugBrowserInfo> debugBrowserInfos = new SmartList<>();
    for (JstdBrowserInfo browserInfo : capturedBrowsers) {
      Pair<JavaScriptDebugEngine, WebBrowser> engine = JavaScriptDebugEngine.Companion.findByBrowserIdOrName(browserInfo.getName());
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
