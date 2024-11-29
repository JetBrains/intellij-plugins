// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

  public @NotNull RemoteDebugTarget getRemoteDebugTarget() {
    return myRemoteDebugTarget;
  }

  public void setRemoteDebugTarget(final @NotNull RemoteDebugTarget remoteDebugTarget) {
    myRemoteDebugTarget = remoteDebugTarget;
  }

  public @NotNull AirMobileDebugTransport getDebugTransport() {
    return myDebugTransport;
  }

  public void setDebugTransport(final @NotNull AirMobileDebugTransport debugTransport) {
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

  @Override
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
