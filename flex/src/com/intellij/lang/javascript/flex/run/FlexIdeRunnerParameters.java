package com.intellij.lang.javascript.flex.run;

import org.jetbrains.annotations.NotNull;

public class FlexIdeRunnerParameters implements Cloneable {

  private @NotNull String myModuleName = "";
  private @NotNull String myBCName = "";
  private boolean myLaunchUrl = false;
  private @NotNull String myUrl = "http://";
  private @NotNull LauncherParameters myLauncherParameters = new LauncherParameters();
  private boolean myRunTrusted = true;

  @NotNull
  public String getModuleName() {
    return myModuleName;
  }

  public void setModuleName(@NotNull final String moduleName) {
    myModuleName = moduleName;
  }

  @NotNull
  public String getBCName() {
    return myBCName;
  }

  public void setBCName(@NotNull final String BCName) {
    myBCName = BCName;
  }

  public boolean isLaunchUrl() {
    return myLaunchUrl;
  }

  public void setLaunchUrl(final boolean launchUrl) {
    myLaunchUrl = launchUrl;
  }

  @NotNull
  public String getUrl() {
    return myUrl;
  }

  public void setUrl(@NotNull final String url) {
    myUrl = url;
  }

  @NotNull
  public LauncherParameters getLauncherParameters() {
    return myLauncherParameters;
  }

  public void setLauncherParameters(@NotNull final LauncherParameters launcherParameters) {
    myLauncherParameters = launcherParameters;
  }

  public boolean isRunTrusted() {
    return myRunTrusted;
  }

  public void setRunTrusted(final boolean runTrusted) {
    myRunTrusted = runTrusted;
  }

  protected FlexIdeRunnerParameters clone() {
    try {
      final FlexIdeRunnerParameters clone = (FlexIdeRunnerParameters)super.clone();
      clone.myLauncherParameters = myLauncherParameters.clone();
      return clone;
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final FlexIdeRunnerParameters that = (FlexIdeRunnerParameters)o;

    if (myLaunchUrl != that.myLaunchUrl) return false;
    if (myRunTrusted != that.myRunTrusted) return false;
    if (!myBCName.equals(that.myBCName)) return false;
    if (!myLauncherParameters.equals(that.myLauncherParameters)) return false;
    if (!myModuleName.equals(that.myModuleName)) return false;
    if (!myUrl.equals(that.myUrl)) return false;

    return true;
  }

  public int hashCode() {
    assert false;
    return super.hashCode();
  }
}
