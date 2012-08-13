package com.google.jstestdriver.idea.execution.settings;

import com.google.common.collect.ImmutableList;
import com.intellij.ide.browsers.BrowsersConfiguration;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Immutable
public class JstdRunSettings {

  public static final BrowsersConfiguration.BrowserFamily DEFAULT_PREFERRED_DEBUG_BROWSER = BrowsersConfiguration.BrowserFamily.CHROME;

  private final TestType myTestType;
  private final String myConfigFile;
  private final String myDirectory;
  private final String myJSFilePath;
  private final ServerType myServerType;
  private final String myServerAddress;
  private final String myTestCaseName;
  private final String myTestMethodName;
  private final ImmutableList<String> myFilesExcludedFromCoverage;
  private final BrowsersConfiguration.BrowserFamily myPreferredDebugBrowser;


  public JstdRunSettings(
    @NotNull TestType testType,
    @NotNull String configFile,
    @NotNull String directory,
    @NotNull String jsFilePath,
    @NotNull String serverAddress,
    @NotNull ServerType serverType,
    @NotNull String testCaseName,
    @NotNull String testMethodName,
    @NotNull ImmutableList<String> filesExcludedFromCoverage,
    @NotNull BrowsersConfiguration.BrowserFamily preferredDebugBrowser) {
    myTestType = testType;
    myConfigFile = configFile;
    myDirectory = directory;
    myJSFilePath = jsFilePath;
    myServerAddress = serverAddress;
    myServerType = serverType;
    myTestCaseName = testCaseName;
    myTestMethodName = testMethodName;
    myFilesExcludedFromCoverage = filesExcludedFromCoverage;
    myPreferredDebugBrowser = preferredDebugBrowser;
  }

  @NotNull
  public TestType getTestType() {
    return myTestType;
  }

  @NotNull
  public String getConfigFile() {
    return myConfigFile;
  }

  @NotNull
  public String getDirectory() {
    return myDirectory;
  }

  @NotNull
  public String getJsFilePath() {
    return myJSFilePath;
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

  @NotNull
  public ImmutableList<String> getFilesExcludedFromCoverage() {
    return myFilesExcludedFromCoverage;
  }

  @NotNull
  public BrowsersConfiguration.BrowserFamily getPreferredDebugBrowser() {
    return myPreferredDebugBrowser;
  }

  public static class Builder {

    private TestType myTestType = TestType.CONFIG_FILE;
    private String myConfigFile = "";
    private String myDirectory = "";
    private String myJSFilePath = "";
    private String myServerAddress = "";
    private ServerType myServerType = ServerType.INTERNAL;
    private String myTestCaseName = "";
    private String myTestMethodName = "";
    private ImmutableList<String> myFilesExcludedFromCoverage = ImmutableList.of();
    private BrowsersConfiguration.BrowserFamily myPreferredDebugBrowser = DEFAULT_PREFERRED_DEBUG_BROWSER;

    public Builder() {
    }

    public Builder(JstdRunSettings runSettings) {
      myTestType = runSettings.getTestType();
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

    public Builder setFilesExcludedFromCoverage(@NotNull List<String> filesExcludedFromCoverage) {
      myFilesExcludedFromCoverage = ImmutableList.copyOf(filesExcludedFromCoverage);
      return this;
    }

    public void setPreferredDebugBrowser(@NotNull BrowsersConfiguration.BrowserFamily preferredDebugBrowser) {
      myPreferredDebugBrowser = preferredDebugBrowser;
    }

    @NotNull
    public JstdRunSettings build() {
      return new JstdRunSettings(
        myTestType,
        myConfigFile,
        myDirectory,
        myJSFilePath,
        myServerAddress,
        myServerType,
        myTestCaseName,
        myTestMethodName,
        myFilesExcludedFromCoverage,
        myPreferredDebugBrowser
      );
    }
  }

}
