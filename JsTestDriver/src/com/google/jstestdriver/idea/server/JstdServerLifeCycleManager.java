package com.google.jstestdriver.idea.server;

import com.google.gson.JsonObject;
import com.google.jstestdriver.idea.common.JstdCommonConstants;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class JstdServerLifeCycleManager implements JstdServerOutputListener {

  private static final Logger LOG = Logger.getInstance(JstdServerLifeCycleManager.class);

  private final List<JstdServerLifeCycleListener> myListeners = new CopyOnWriteArrayList<>();
  private final List<Disposable> myDisposables = new CopyOnWriteArrayList<>();
  private final Map<String, JstdBrowserInfo> myCapturedBrowsers = ContainerUtil.newHashMap();
  private boolean myServerStarted = false;
  private boolean myServerStopped = false;

  public void addListener(@NotNull final JstdServerLifeCycleListener listener, @NotNull Disposable disposable) {
    myListeners.add(listener);
    if (myServerStarted) {
      listener.onServerStarted();
      for (JstdBrowserInfo browserInfo : myCapturedBrowsers.values()) {
        listener.onBrowserCaptured(browserInfo);
      }
    }
    Disposable d = new Disposable() {
      @Override
      public void dispose() {
        myListeners.remove(listener);
      }
    };
    myDisposables.add(d);
    Disposer.register(disposable, d);
  }

  public void removeListener(@NotNull JstdServerLifeCycleListener listener) {
    myListeners.remove(listener);
  }

  @NotNull
  public Collection<JstdBrowserInfo> getCapturedBrowsers() {
    return Collections.unmodifiableCollection(myCapturedBrowsers.values());
  }

  public boolean isServerStarted() {
    return myServerStarted;
  }

  public boolean isServerStopped() {
    return myServerStopped;
  }

  @Override
  public void onOutputAvailable(@NotNull String text, @NotNull Key outputType) {}

  @Override
  public void onEvent(@NotNull final JsonObject obj) {
    UIUtil.invokeLaterIfNeeded(() -> {
      String type = JsonUtil.getChildAsString(obj, JstdCommonConstants.EVENT_TYPE);
      if (JstdCommonConstants.SERVER_STARTED.equals(type)) {
        onServerStarted();
      }
      else if (JstdCommonConstants.SERVER_STOPPED.equals(type)) {
        onServerStopped();
      }
      else if (JstdCommonConstants.BROWSER_CAPTURED.equals(type)) {
        onBrowserCaptured(obj);
      }
      else if (JstdCommonConstants.BROWSER_PANICKED.equals(type)) {
        onBrowserPanicked(obj);
      }
    });
  }

  private void onServerStarted() {
    if (myServerStarted) {
      LOG.warn("[on server started] Jstd server already started");
    }
    LOG.info("Jstd server started");
    myServerStarted = true;
    myServerStopped = false;
    for (JstdServerLifeCycleListener listener : myListeners) {
      listener.onServerStarted();
    }
  }

  private void onServerStopped() {
    if (myServerStopped) {
      LOG.warn("[on server stopped] Jstd server already stopped");
    }
    LOG.info("Jstd server stopped");
    myServerStarted = false;
    myServerStopped = true;
    for (JstdServerLifeCycleListener listener : myListeners) {
      listener.onServerStopped();
    }
  }

  private void onBrowserCaptured(@NotNull JsonObject obj) {
    JstdBrowserInfo info = getBrowserInfo(obj);
    if (info == null) {
      LOG.warn("No browser info parsed, aborting...");
      return;
    }
    if (myCapturedBrowsers.put(info.getId(), info) != null) {
      LOG.warn("Capturing already captured browser: " + info);
    }
    for (JstdServerLifeCycleListener listener : myListeners) {
      listener.onBrowserCaptured(info);
    }
  }

  private void onBrowserPanicked(@NotNull JsonObject obj) {
    JstdBrowserInfo info = getBrowserInfo(obj);
    if (info == null) {
      LOG.warn("No browser info parsed, aborting...");
      return;
    }
    if (myCapturedBrowsers.remove(info.getId()) == null) {
      LOG.warn("Not-captured browser panicked: " + info);
    }
    for (JstdServerLifeCycleListener listener : myListeners) {
      listener.onBrowserPanicked(info);
    }
  }

  @Nullable
  private static JstdBrowserInfo getBrowserInfo(@NotNull JsonObject obj) {
    JsonObject browserInfoObj = JsonUtil.getChildAsObject(obj, JstdCommonConstants.BROWSER_INFO);
    if (browserInfoObj != null) {
      String id = JsonUtil.getChildAsString(browserInfoObj, JstdCommonConstants.BROWSER_INFO_ID);
      String name = JsonUtil.getChildAsString(browserInfoObj, JstdCommonConstants.BROWSER_INFO_NAME);
      if (id != null && name != null) {
        return new JstdBrowserInfo(id, name);
      }
    }
    return null;
  }

  public void onTerminated(final int exitCode) {
    UIUtil.invokeLaterIfNeeded(() -> {
      if (!myServerStopped) {
        onServerStopped();
      }
      for (JstdServerLifeCycleListener listener : myListeners) {
        listener.onServerTerminated(exitCode);
      }
      dispose();
    });
  }

  private void dispose() {
    myListeners.clear();
    myCapturedBrowsers.clear();
    for (Disposable disposable : myDisposables) {
      Disposer.dispose(disposable);
    }
    myDisposables.clear();
  }
}
