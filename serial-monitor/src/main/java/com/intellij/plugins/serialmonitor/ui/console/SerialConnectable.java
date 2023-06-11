package com.intellij.plugins.serialmonitor.ui.console;

import com.intellij.execution.console.DuplexConsoleListener;
import com.intellij.execution.console.DuplexConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.plugins.serialmonitor.SerialPortProfile;
import com.intellij.plugins.serialmonitor.service.SerialConnectionListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.charset.Charset;

public abstract class SerialConnectable<T extends ConsoleView> extends DuplexConsoleView<T, HexConsoleView> implements Disposable {
  protected SerialConnectable(@NotNull T primaryConsoleView,
                              @NotNull HexConsoleView secondaryConsoleView,
                              @Nullable String stateStorageKey) {
    super(primaryConsoleView, secondaryConsoleView, stateStorageKey);
  }

  //todo refactor out
  public abstract  boolean isConnected();

  public abstract boolean isPortValid();

  public abstract void openConnectionTab(boolean doConnect);

  public abstract boolean isLoading();

  public abstract void reconnect();

  public abstract SerialPortProfile getPortProfile();

  @NotNull
  public abstract Charset getCharset();

  public abstract void setPortStateListener(SerialConnectionListener stateListener);

}
