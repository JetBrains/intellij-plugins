package com.intellij.lang.javascript.flex.run;

import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 * Date: Dec 27, 2007
 * Time: 11:54:37 PM
 */
public class FlexRunnerParameters implements Cloneable {

  public enum RunMode {
    HtmlOrSwfFile, Url, MainClass, ConnectToRunningFlashPlayer
  }

  public enum LauncherType {
    OSDefault, Browser, Player
  }

  private @NotNull String myModuleName = "";
  private @NotNull RunMode myRunMode = RunMode.HtmlOrSwfFile;
  private @NotNull String myHtmlOrSwfFilePath = "";
  private @NotNull String myUrlToLaunch = "http://";
  private @NotNull String myMainClassName = "";
  private boolean myRunTrusted = true;
  private @NotNull LauncherType myLauncherType = LauncherType.OSDefault;
  private @NotNull BrowsersConfiguration.BrowserFamily myBrowserFamily = BrowsersConfiguration.BrowserFamily.FIREFOX;
  private @NotNull String myPlayerPath = SystemInfo.isMac
                                         ? "/Applications/Flash Player.app/Contents/MacOS/Flash Player"
                                         : SystemInfo.isWindows ? "FlashPlayer.exe" : "/usr/bin/flashplayer";
  private @NotNull String myDebuggerSdkRaw = FlexSdkComboBoxWithBrowseButton.MODULE_SDK_KEY;

  public FlexRunnerParameters() {
  }

  @Attribute("module_name")
  @NotNull
  public String getModuleName() {
    return myModuleName;
  }

  public void setModuleName(final @NotNull String moduleName) {
    myModuleName = moduleName;
  }

  @NotNull
  @Attribute("run_mode")
  public String getRunModeRaw() {
    return myRunMode.name();
  }

  public void setRunModeRaw(final @NotNull String runMode) {
    try {
      myRunMode = RunMode.valueOf(runMode);
    }
    catch (IllegalArgumentException e) {
      myRunMode = RunMode.HtmlOrSwfFile;
    }
  }

  @NotNull
  @Transient
  public RunMode getRunMode() {
    return myRunMode;
  }

  public void setRunMode(final @NotNull RunMode runMode) {
    myRunMode = runMode;
  }

  @NotNull
  @Attribute("html_or_swf_file_path")
  public String getHtmlOrSwfFilePath() {
    return myHtmlOrSwfFilePath;
  }

  public void setHtmlOrSwfFilePath(final @NotNull String htmlOrSwfFilePath) {
    myHtmlOrSwfFilePath = htmlOrSwfFilePath;
  }

  @NotNull
  @Attribute("url_to_launch")
  public String getUrlToLaunch() {
    return myUrlToLaunch;
  }

  public void setUrlToLaunch(final @NotNull String urlToLaunch) {
    myUrlToLaunch = urlToLaunch;
  }

  @NotNull
  @Attribute("main_class_name")
  public String getMainClassName() {
    return myMainClassName;
  }

  public void setMainClassName(final @NotNull String mainClassName) {
    myMainClassName = mainClassName;
  }

  @Attribute("run_trusted")
  public boolean isRunTrusted() {
    return myRunTrusted;
  }

  public void setRunTrusted(final boolean runTrusted) {
    myRunTrusted = runTrusted;
  }

  @NotNull
  @Attribute("launcher_type")
  public String getLauncherTypeRaw() {
    return myLauncherType.name();
  }

  public void setLauncherTypeRaw(final @NotNull String launcherType) {
    try {
      myLauncherType = LauncherType.valueOf(launcherType);
    }
    catch (IllegalArgumentException e) {
      myLauncherType = LauncherType.OSDefault;
    }
  }

  @NotNull
  @Transient
  public LauncherType getLauncherType() {
    return myLauncherType;
  }

  public void setLauncherType(final @NotNull LauncherType launcherType) {
    myLauncherType = launcherType;
  }

  @NotNull
  @Attribute("browser_family")
  public String getBrowserFamilyRaw() {
    return myBrowserFamily.name();
  }

  public void setBrowserFamilyRaw(final @NotNull String browserFamily) {
    try {
      myBrowserFamily = BrowsersConfiguration.BrowserFamily.valueOf(browserFamily);
    }
    catch (IllegalArgumentException e) {
      myBrowserFamily = BrowsersConfiguration.BrowserFamily.FIREFOX;
    }
  }

  @NotNull
  @Transient
  public BrowsersConfiguration.BrowserFamily getBrowserFamily() {
    return myBrowserFamily;
  }

  public void setBrowserFamily(final @NotNull BrowsersConfiguration.BrowserFamily browserFamily) {
    myBrowserFamily = browserFamily;
  }

  @Attribute("player_path")
  @NotNull
  public String getPlayerPath() {
    return myPlayerPath;
  }

  public void setPlayerPath(final @NotNull String playerPath) {
    myPlayerPath = playerPath;
  }

  @Attribute("debugger_sdk")
  @NotNull
  public String getDebuggerSdkRaw() {
    return myDebuggerSdkRaw;
  }

  public void setDebuggerSdkRaw(final @NotNull String debuggerSdkRaw) {
    myDebuggerSdkRaw = debuggerSdkRaw;
  }

  @Override
  @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException"})
  public FlexRunnerParameters clone() {
    try {
      return (FlexRunnerParameters)super.clone();
    }
    catch (CloneNotSupportedException e) {
      return null;
    }
  }
}
