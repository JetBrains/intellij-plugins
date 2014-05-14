package com.google.jstestdriver.idea.server;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JstdServerInfo {

  private final String myServerUrl;
  private final List<JstdBrowserInfo> myCapturedBrowsers;

  public JstdServerInfo(@NotNull String serverUrl, @NotNull List<JstdBrowserInfo> capturedBrowsers) {
    myServerUrl = serverUrl;
    myCapturedBrowsers = ImmutableList.copyOf(capturedBrowsers);
  }

  public String getServerUrl() {
    return myServerUrl;
  }

  @NotNull
  public List<JstdBrowserInfo> getCapturedBrowsers() {
    return myCapturedBrowsers;
  }
}
