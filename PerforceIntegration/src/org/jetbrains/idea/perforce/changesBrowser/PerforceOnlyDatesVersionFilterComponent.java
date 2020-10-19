package org.jetbrains.idea.perforce.changesBrowser;

import com.intellij.openapi.vcs.versionBrowser.StandardVersionFilterComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PerforceOnlyDatesVersionFilterComponent extends StandardVersionFilterComponent<PerforceChangeBrowserSettings> {
  public PerforceOnlyDatesVersionFilterComponent() {
    super(true);
    disableVersionNumbers();
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return (JPanel) getStandardPanel();
  }
}
