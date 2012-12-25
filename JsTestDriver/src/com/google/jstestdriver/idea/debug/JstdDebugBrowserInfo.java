package com.google.jstestdriver.idea.debug;

import com.google.common.collect.Lists;
import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.server.JstdServerState;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
* @author Sergey Simonchik
*/
public class JstdDebugBrowserInfo<Connection> {

  private final JSDebugEngine<Connection> myDebugEngine;
  private final String myCapturedBrowserUrl;
  private final SlaveBrowser mySlaveBrowser;

  private JstdDebugBrowserInfo(@NotNull JSDebugEngine<Connection> debugEngine,
                               @NotNull String capturedBrowserUrl,
                               @NotNull SlaveBrowser slaveBrowser) {
    myCapturedBrowserUrl = capturedBrowserUrl;
    myDebugEngine = debugEngine;
    mySlaveBrowser = slaveBrowser;
  }

  @NotNull
  public JSDebugEngine<Connection> getDebugEngine() {
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
  public static <Connection> JstdDebugBrowserInfo<Connection> build(@NotNull JstdRunSettings settings) {
    JstdServerState jstdServerState = JstdServerState.getInstance();
    CapturedBrowsers browsers = jstdServerState.getCaptured();
    if (browsers == null) {
      return null;
    }
    JSDebugEngine<Connection>[] engines = listDebugEngines();
    List<JstdDebugBrowserInfo<Connection>> debugBrowserInfos = Lists.newArrayList();
    for (SlaveBrowser slaveBrowser : browsers.getSlaveBrowsers()) {
      String browserName = slaveBrowser.getBrowserInfo().getName();
      for (JSDebugEngine<Connection> engine : engines) {
        if (engine.getId().equalsIgnoreCase(browserName)) {
          debugBrowserInfos.add(new JstdDebugBrowserInfo<Connection>(engine, slaveBrowser.getCaptureUrl(), slaveBrowser));
        }
      }
    }
    if (debugBrowserInfos.size() == 1) {
      return debugBrowserInfos.get(0);
    }
    if (debugBrowserInfos.size() > 1) {
      BrowsersConfiguration.BrowserFamily preferredBrowser = settings.getPreferredDebugBrowser();
      for (JstdDebugBrowserInfo<Connection> info : debugBrowserInfos) {
        if (info.getDebugEngine().getBrowserFamily() == preferredBrowser) {
          return info;
        }
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private static <C> JSDebugEngine<C>[] listDebugEngines() {
    return (JSDebugEngine<C>[])JSDebugEngine.getEngines();
  }

}
