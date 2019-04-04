package com.google.jstestdriver.idea.execution.settings.ui;

import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * All methods should be called on EDT.
 */
public abstract class AbstractRunSettingsSection implements RunSettingsSection {

  private JComponent myComponent;
  private JComponent myAnchor;

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

  @Override
  public JComponent getAnchor() {
    return myAnchor;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    myAnchor = anchor;
  }
}
