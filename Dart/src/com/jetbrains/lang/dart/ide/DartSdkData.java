package com.jetbrains.lang.dart.ide;

public class DartSdkData {
  private String homePath = "";
  private String version = "";

  public DartSdkData(String homePath, String version) {
    this.homePath = homePath;
    this.version = version;
  }

  public String getHomePath() {
    return homePath;
  }

  public String getVersion() {
    return version;
  }
}
