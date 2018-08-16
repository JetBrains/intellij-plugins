package com.intellij.lang.javascript.flex.run;

import com.intellij.lang.javascript.flex.actions.airpackage.AirPackageUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileDebugTransport;

public class RemoteFlashRunnerParameters extends BCBasedRunnerParameters {
  public enum RemoteDebugTarget {
    Computer, AndroidDevice, iOSDevice
  }

  private @NotNull RemoteDebugTarget myRemoteDebugTarget = RemoteDebugTarget.Computer;
  private @NotNull AirMobileDebugTransport myDebugTransport = AirMobileDebugTransport.USB;
  private int myUsbDebugPort = AirPackageUtil.DEBUG_PORT_DEFAULT;

  @NotNull
  public RemoteDebugTarget getRemoteDebugTarget() {
    return myRemoteDebugTarget;
  }

  public void setRemoteDebugTarget(@NotNull final RemoteDebugTarget remoteDebugTarget) {
    myRemoteDebugTarget = remoteDebugTarget;
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

  @Override
  protected RemoteFlashRunnerParameters clone() {
    return (RemoteFlashRunnerParameters)super.clone();
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    final RemoteFlashRunnerParameters that = (RemoteFlashRunnerParameters)o;

    if (myUsbDebugPort != that.myUsbDebugPort) return false;
    if (myDebugTransport != that.myDebugTransport) return false;
    if (myRemoteDebugTarget != that.myRemoteDebugTarget) return false;

    return true;
  }
}
