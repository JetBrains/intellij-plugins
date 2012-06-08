package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.hooks.ServerListener;

/**
 * @author Sergey Simonchik
 */
public class ServerAdapter implements ServerListener {
  @Override
  public void serverStarted() {}

  @Override
  public void serverStopped() {}

  @Override
  public void browserCaptured(BrowserInfo info) {}

  @Override
  public void browserPanicked(BrowserInfo info) {}
}
