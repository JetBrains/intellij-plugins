package com.intellij.plugins.serialmonitor.service;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SerialConnectionListener {

  enum PortStatus {DISCONNECTED, CONNECTING, CONNECTED, FAILURE}

  void updateStatus(@NotNull SerialConnectionListener.PortStatus status);
}
