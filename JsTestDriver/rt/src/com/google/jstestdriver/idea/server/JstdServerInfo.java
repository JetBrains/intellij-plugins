package com.google.jstestdriver.idea.server;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JstdServerInfo {

  private final String myServerUrl;
  private final List<JstdBrowserInfo> myCapturedBrowsers;

  public JstdServerInfo(@NotNull String serverUrl, @NotNull List<JstdBrowserInfo> capturedBrowsers) {
    this.myServerUrl = serverUrl;
    this.myCapturedBrowsers = new ImmutableList.Builder<JstdBrowserInfo>().addAll(capturedBrowsers).build();
  }

  public String getServerUrl() {
    return myServerUrl;
  }

  @NotNull
  public List<JstdBrowserInfo> getCapturedBrowsers() {
    return myCapturedBrowsers;
  }
}
