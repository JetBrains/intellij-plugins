package com.google.jstestdriver.idea.execution.settings;

import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public class JstdRunSettings {

  private final TestType myTestType;
  private final JstdConfigType myJstdConfigType;
  private final String myConfigFile;
  private final String myDirectory;
  private final String myJSFilePath;
  private final ServerType myServerType;
  private final String myServerAddress;
  private final String myTestCaseName;
  private final String myTestMethodName;

  public JstdRunSettings(
      @NotNull TestType testType,
      @NotNull JstdConfigType jstdConfigType,
      @NotNull String configFile,
      @NotNull String directory,
      @NotNull String jsFilePath,
      @NotNull String serverAddress,
      @NotNull ServerType serverType,
      @NotNull String testCaseName,
      @NotNull String testMethodName
  ) {
    myTestType = testType;
    myJstdConfigType = jstdConfigType;
    myConfigFile = configFile;
    myDirectory = directory;
    myJSFilePath = jsFilePath;
    myServerAddress = serverAddress;
    myServerType = serverType;
    myTestCaseName = testCaseName;
    myTestMethodName = testMethodName;
  }

  @NotNull
  public TestType getTestType() {
    return myTestType;
  }

  public JstdConfigType getConfigType() {
    return myJstdConfigType;
  }

  @NotNull
  public String getConfigFile() {
    return myConfigFile;
  }

  @NotNull
  public String getDirectory() {
    return myDirectory;
  }

  public String getJsFilePath() {
    return myJSFilePath;
  }

  public boolean isAllInDirectory() {
    return myTestType == TestType.ALL_CONFIGS_IN_DIRECTORY;
  }

  @NotNull
  public String getServerAddress() {
    return myServerAddress;
  }

  @NotNull
  public ServerType getServerType() {
    return myServerType;
  }

  public boolean isExternalServerType() {
    return myServerType == ServerType.EXTERNAL;
  }

  @NotNull
  public String getTestCaseName() {
    return myTestCaseName;
  }

  public String getTestMethodName() {
    return myTestMethodName;
  }

  public static class Builder {

    private TestType myTestType = TestType.CONFIG_FILE;
    private JstdConfigType myConfigType = JstdConfigType.GENERATED;
    private String myConfigFile = "";
    private String myDirectory = "";
    private String myJSFilePath = "";
    private String myServerAddress = "";
    private ServerType myServerType = ServerType.INTERNAL;
    private String myTestCaseName = "";
    private String myTestMethodName = "";

    public Builder() {
    }

    public Builder(JstdRunSettings runSettings) {
      myTestType = runSettings.getTestType();
      myConfigType = runSettings.getConfigType();
      myConfigFile = runSettings.getConfigFile();
      myDirectory = runSettings.getDirectory();
      myJSFilePath = runSettings.getJsFilePath();
      myServerAddress = runSettings.getServerAddress();
      myServerType = runSettings.getServerType();
      myTestCaseName = runSettings.getTestCaseName();
      myTestMethodName = runSettings.getTestMethodName();
    }

    public Builder setTestType(@NotNull TestType testType) {
      myTestType = testType;
      return this;
    }

    public void setConfigType(@NotNull JstdConfigType jstdConfigType) {
      myConfigType = jstdConfigType;
    }

    public Builder setConfigFile(@NotNull String configFile) {
      this.myConfigFile = configFile;
      return this;
    }

    public Builder setDirectory(@NotNull String directory) {
      myDirectory = directory;
      return this;
    }

    public Builder setJSFilePath(@NotNull String jsFilePath) {
      myJSFilePath = jsFilePath;
      return this;
    }

    public Builder setServerAddress(@NotNull String serverAddress) {
      myServerAddress = serverAddress;
      return this;
    }

    public Builder setServerType(@NotNull ServerType serverType) {
      myServerType = serverType;
      return this;
    }

    public Builder setTestCaseName(String testCaseName) {
      myTestCaseName = testCaseName;
      return this;
    }

    public Builder setTestMethodName(String testMethodName) {
      myTestMethodName = testMethodName;
      return this;
    }

    @NotNull
    public JstdRunSettings build() {
      return new JstdRunSettings(myTestType, myConfigType, myConfigFile, myDirectory, myJSFilePath, myServerAddress, myServerType, myTestCaseName, myTestMethodName);
    }
  }
}
