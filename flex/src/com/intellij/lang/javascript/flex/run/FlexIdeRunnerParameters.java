package com.intellij.lang.javascript.flex.run;

import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.flex.run.AirMobileRunnerParameters.AirMobileDebugTransport;
import static com.intellij.lang.javascript.flex.run.AirMobileRunnerParameters.AirMobileRunTarget;
import static com.intellij.lang.javascript.flex.run.AirMobileRunnerParameters.Emulator;

public class FlexIdeRunnerParameters extends BCBasedRunnerParameters implements Cloneable {

  private boolean myLaunchUrl = false;
  private @NotNull String myUrl = "http://";

  private @NotNull LauncherParameters myLauncherParameters = new LauncherParameters();
  private boolean myRunTrusted = true;

  private @NotNull String myAdlOptions = "";
  private @NotNull String myAirProgramParameters = "";

  private @NotNull AirMobileRunTarget myMobileRunTarget = AirMobileRunTarget.Emulator;
  private @NotNull Emulator myEmulator = Emulator.NexusOne;
  private int myScreenWidth = 0;
  private int myScreenHeight = 0;
  private int myFullScreenWidth = 0;
  private int myFullScreenHeight = 0;
  private @NotNull AirMobileDebugTransport myDebugTransport = AirMobileDebugTransport.USB;
  private int myUsbDebugPort = MobileAirUtil.DEBUG_PORT_DEFAULT;
  private @NotNull String myEmulatorAdlOptions = "";

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

  @NotNull
  public String getAdlOptions() {
    return myAdlOptions;
  }

  public void setAdlOptions(@NotNull final String adlOptions) {
    myAdlOptions = adlOptions;
  }

  @NotNull
  public String getAirProgramParameters() {
    return myAirProgramParameters;
  }

  public void setAirProgramParameters(@NotNull final String airProgramParameters) {
    myAirProgramParameters = airProgramParameters;
  }

  @NotNull
  public AirMobileRunTarget getMobileRunTarget() {
    return myMobileRunTarget;
  }

  public void setMobileRunTarget(@NotNull final AirMobileRunTarget mobileRunTarget) {
    myMobileRunTarget = mobileRunTarget;
  }

  @NotNull
  public Emulator getEmulator() {
    return myEmulator;
  }

  public void setEmulator(@NotNull final Emulator emulator) {
    myEmulator = emulator;
  }

  public int getScreenWidth() {
    return myScreenWidth;
  }

  public void setScreenWidth(final int screenWidth) {
    myScreenWidth = screenWidth;
  }

  public int getScreenHeight() {
    return myScreenHeight;
  }

  public void setScreenHeight(final int screenHeight) {
    myScreenHeight = screenHeight;
  }

  public int getFullScreenWidth() {
    return myFullScreenWidth;
  }

  public void setFullScreenWidth(final int fullScreenWidth) {
    myFullScreenWidth = fullScreenWidth;
  }

  public int getFullScreenHeight() {
    return myFullScreenHeight;
  }

  public void setFullScreenHeight(final int fullScreenHeight) {
    myFullScreenHeight = fullScreenHeight;
  }

  @NotNull
  public AirMobileDebugTransport getDebugTransport() {
    return myDebugTransport;
  }

  public void setDebugTransport(@NotNull final AirMobileDebugTransport debugTransport) {
    myDebugTransport = debugTransport;
  }

  public int getUsbDebugPort() {
    return myUsbDebugPort;
  }

  public void setUsbDebugPort(final int usbDebugPort) {
    myUsbDebugPort = usbDebugPort;
  }

  @NotNull
  public String getEmulatorAdlOptions() {
    return myEmulatorAdlOptions;
  }

  public void setEmulatorAdlOptions(@NotNull final String emulatorAdlOptions) {
    myEmulatorAdlOptions = emulatorAdlOptions;
  }

  protected FlexIdeRunnerParameters clone() {
    final FlexIdeRunnerParameters clone = (FlexIdeRunnerParameters)super.clone();
    clone.myLauncherParameters = myLauncherParameters.clone();
    return clone;
  }

  public boolean equals(final Object o) {
    if (!super.equals(o)) return false;

    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final FlexIdeRunnerParameters that = (FlexIdeRunnerParameters)o;

    if (myFullScreenHeight != that.myFullScreenHeight) return false;
    if (myFullScreenWidth != that.myFullScreenWidth) return false;
    if (myLaunchUrl != that.myLaunchUrl) return false;
    if (myRunTrusted != that.myRunTrusted) return false;
    if (myScreenHeight != that.myScreenHeight) return false;
    if (myScreenWidth != that.myScreenWidth) return false;
    if (myUsbDebugPort != that.myUsbDebugPort) return false;
    if (!myAdlOptions.equals(that.myAdlOptions)) return false;
    if (!myAirProgramParameters.equals(that.myAirProgramParameters)) return false;
    if (myDebugTransport != that.myDebugTransport) return false;
    if (myEmulator != that.myEmulator) return false;
    if (!myEmulatorAdlOptions.equals(that.myEmulatorAdlOptions)) return false;
    if (!myLauncherParameters.equals(that.myLauncherParameters)) return false;
    if (myMobileRunTarget != that.myMobileRunTarget) return false;
    if (!myUrl.equals(that.myUrl)) return false;

    return true;
  }
}
