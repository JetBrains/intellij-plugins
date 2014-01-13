package com.google.jstestdriver.idea.debug;

import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.server.JstdServerState;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
* @author Sergey Simonchik
*/
public class JstdDebugBrowserInfo {

  private final JSDebugEngine myDebugEngine;
  private final String myCapturedBrowserUrl;
  private final SlaveBrowser mySlaveBrowser;

  private JstdDebugBrowserInfo(@NotNull JSDebugEngine debugEngine,
                               @NotNull String capturedBrowserUrl,
                               @NotNull SlaveBrowser slaveBrowser) {
    myCapturedBrowserUrl = capturedBrowserUrl;
    myDebugEngine = debugEngine;
    mySlaveBrowser = slaveBrowser;
  }

  @NotNull
  public JSDebugEngine getDebugEngine() {
    return myDebugEngine;
  }

  @NotNull
  public String getCapturedBrowserUrl() {
    return myCapturedBrowserUrl;
  }

  public void fixIfChrome(@NotNull ProcessHandler processHandler) {
    if (!(myDebugEngine.getBrowserFamily().equals(BrowsersConfiguration.BrowserFamily.CHROME))) {
      return;
    }
    final AtomicBoolean done = new AtomicBoolean(false);
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        while (!done.get()) {
          mySlaveBrowser.heartBeat();
          try {
            //noinspection BusyWait
            Thread.sleep(5000);
          }
          catch (InterruptedException ignored) {
          }
        }
      }
    });
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(ProcessEvent event) {
        done.set(true);
      }
    });
  }

  @Nullable
  public static JstdDebugBrowserInfo build(@NotNull JstdRunSettings settings) {
    JstdServerState jstdServerState = JstdServerState.getInstance();
    CapturedBrowsers browsers = jstdServerState.getCaptured();
    if (browsers == null) {
      return null;
    }

    List<JstdDebugBrowserInfo> debugBrowserInfos = new SmartList<JstdDebugBrowserInfo>();
    for (SlaveBrowser slaveBrowser : browsers.getSlaveBrowsers()) {
      JSDebugEngine engine = JSDebugEngine.findByBrowserName(slaveBrowser.getBrowserInfo().getName());
      if (engine != null) {
        debugBrowserInfos.add(new JstdDebugBrowserInfo(engine, slaveBrowser.getCaptureUrl(), slaveBrowser));
      }
    }
    if (debugBrowserInfos.size() == 1) {
      return debugBrowserInfos.get(0);
    }
    if (debugBrowserInfos.size() > 1) {
      WebBrowser preferredBrowser = settings.getPreferredDebugBrowser();
      for (JstdDebugBrowserInfo info : debugBrowserInfos) {
        if (info.getDebugEngine().getWebBrowser().equals(preferredBrowser)) {
          return info;
        }
      }
    }
    return null;
  }
}
