package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;

import javax.swing.*;

import com.intellij.ui.PanelWithAnchor;
import org.jetbrains.annotations.NotNull;

interface RunSettingsSection extends PanelWithAnchor {

  void resetFrom(@NotNull JstdRunSettings runSettings);

  void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder);

  @NotNull
  JComponent getComponent(@NotNull CreationContext creationContext);
}
