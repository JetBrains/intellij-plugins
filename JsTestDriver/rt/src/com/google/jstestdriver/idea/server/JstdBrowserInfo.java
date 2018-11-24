package com.google.jstestdriver.idea.server;

public class JstdBrowserInfo {

  private final String name;
  private final String version;
  private final String os;

  public JstdBrowserInfo(String name, String version, String os) {
    this.name = name;
    this.version = version;
    this.os = os;
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public String getOs() {
    return os;
  }
}
