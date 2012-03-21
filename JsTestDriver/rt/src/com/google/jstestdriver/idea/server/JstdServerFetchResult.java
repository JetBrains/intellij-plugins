package com.google.jstestdriver.idea.server;

public class JstdServerFetchResult {
  private final JstdServerInfo myServerInfo;
  private final String myErrorMessage;

  public JstdServerFetchResult(JstdServerInfo serverInfo, String errorMessage) {
    this.myErrorMessage = errorMessage;
    this.myServerInfo = serverInfo;
  }

  public JstdServerInfo getServerInfo() {
    return myServerInfo;
  }

  public boolean isError() {
    return myErrorMessage != null;
  }

  public String getErrorMessage() {
    return myErrorMessage;
  }

  public static JstdServerFetchResult fromErrorMessage(String errorMessage) {
    return new JstdServerFetchResult(null, errorMessage);
  }

  public static JstdServerFetchResult fromServerInfo(JstdServerInfo serverInfo) {
    return new JstdServerFetchResult(serverInfo, null);
  }
}
