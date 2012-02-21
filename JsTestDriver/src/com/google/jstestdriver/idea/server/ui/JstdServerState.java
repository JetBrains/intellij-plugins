package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.hooks.ServerListener;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
* @author Sergey Simonchik
*/
public class JstdServerState implements ServerListener {

  private volatile boolean myServerRunning = false;
  private final Map<String, BrowserInfo> myCapturedBrowsers = new ConcurrentHashMap<String, BrowserInfo>();
  private final Map<ServerListener, Object> myServerListeners = new IdentityHashMap<ServerListener, Object>();

  @Override
  public void serverStarted() {
    myServerRunning = true;
    for (ServerListener serverListener : myServerListeners.keySet()) {
      serverListener.serverStarted();
    }
  }

  @Override
  public void serverStopped() {
    myServerRunning = false;
    for (ServerListener serverListener : myServerListeners.keySet()) {
      serverListener.serverStopped();
    }
  }

  @Override
  public void browserCaptured(BrowserInfo info) {
    myCapturedBrowsers.put(info.getName(), info);
    for (ServerListener serverListener : myServerListeners.keySet()) {
      serverListener.browserCaptured(info);
    }
  }

  @Override
  public void browserPanicked(BrowserInfo info) {
    myCapturedBrowsers.remove(info.getName());
    for (ServerListener serverListener : myServerListeners.keySet()) {
      serverListener.browserPanicked(info);
    }
  }

  public boolean isServerRunning() {
    return myServerRunning;
  }

  public Collection<BrowserInfo> getCapturedBrowsers() {
    return myCapturedBrowsers.values();
  }

  public void addServerListener(@NotNull ServerListener serverListener) {
    myServerListeners.put(serverListener, true);
  }

  public void removeServerListener(@NotNull ServerListener serverListener) {
    myServerListeners.remove(serverListener);
  }
}
