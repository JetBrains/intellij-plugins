package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

interface RunSettingsSection {

  void resetFrom(JstdRunSettings runSettings);

  void applyTo(JstdRunSettings.Builder runSettingsBuilder);

  @NotNull
  JComponent getComponent(@NotNull CreationContext creationContext);
}
