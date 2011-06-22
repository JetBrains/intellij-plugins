package com.google.jstestdriver.idea.execution.settings.ui;

import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;

/**
 * All methods should be called on AWT Thread.
 */
abstract class AbstractRunSettingsSection implements RunSettingsSection {

  private JComponent myComponent;

  @NotNull
  @Override
  public final JComponent getComponent(@NotNull CreationContext creationContext) {
    if (myComponent == null) {
      myComponent = createComponent(creationContext);
    }
    return myComponent;
  }

  @NotNull
  protected abstract JComponent createComponent(@NotNull CreationContext creationContext);

}
