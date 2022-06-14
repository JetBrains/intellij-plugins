package com.intellij.plugins.serialmonitor.service;

import com.intellij.util.messages.Topic;

public interface SerialSettingsChangeListener {
  Topic<SerialSettingsChangeListener> TOPIC = new Topic<>(SerialSettingsChangeListener.class);
  void settingsChanged();
}
