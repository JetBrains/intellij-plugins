package com.intellij.lang.javascript.flex.run;

import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

public class LauncherParameters implements Cloneable {

  public static enum LauncherType {
    OSDefault, Browser, Player
  }

  private @NotNull LauncherType myLauncherType = LauncherType.OSDefault;
  private @NotNull BrowsersConfiguration.BrowserFamily myBrowserFamily = BrowsersConfiguration.BrowserFamily.FIREFOX;
  private @NotNull String myPlayerPath = SystemInfo.isMac ? "/Applications/Flash Player Debugger.app"
                                                          : SystemInfo.isWindows ? "FlashPlayerDebugger.exe"
                                                                                 : "/usr/bin/flashplayerdebugger";

  public LauncherParameters() {
  }

  public LauncherParameters(@NotNull final LauncherType launcherType,
                            @NotNull final BrowsersConfiguration.BrowserFamily browserFamily,
                            @NotNull final String playerPath) {
    myLauncherType = launcherType;
    myBrowserFamily = browserFamily;
    myPlayerPath = playerPath;
  }

  public String getPresentableText() {
    switch (myLauncherType) {
      case OSDefault:
        return FlexBundle.message("system.default.application");
      case Browser:
        return myBrowserFamily.getName();
      case Player:
        return FileUtil.toSystemDependentName(myPlayerPath);
      default:
        assert false;
        return "";
    }
  }

  @NotNull
  public LauncherType getLauncherType() {
    return myLauncherType;
  }

  public void setLauncherType(@NotNull final LauncherType launcherType) {
    myLauncherType = launcherType;
  }

  @NotNull
  public BrowsersConfiguration.BrowserFamily getBrowserFamily() {
    return myBrowserFamily;
  }

  public void setBrowserFamily(@NotNull final BrowsersConfiguration.BrowserFamily browserFamily) {
    myBrowserFamily = browserFamily;
  }

  @NotNull
  public String getPlayerPath() {
    return myPlayerPath;
  }

  public void setPlayerPath(@NotNull final String playerPath) {
    myPlayerPath = FileUtil.toSystemIndependentName(playerPath);
  }

  public LauncherParameters clone() {
    try {
      return (LauncherParameters)super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final LauncherParameters that = (LauncherParameters)o;

    if (myBrowserFamily != that.myBrowserFamily) return false;
    if (myLauncherType != that.myLauncherType) return false;
    if (!myPlayerPath.equals(that.myPlayerPath)) return false;

    return true;
  }

  public int hashCode() {
    assert false;
    return super.hashCode();
  }
}
