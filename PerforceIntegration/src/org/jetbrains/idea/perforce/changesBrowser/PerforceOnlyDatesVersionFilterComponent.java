package org.jetbrains.idea.perforce.changesBrowser;

import com.intellij.openapi.vcs.versionBrowser.StandardVersionFilterComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PerforceOnlyDatesVersionFilterComponent extends StandardVersionFilterComponent<PerforceChangeBrowserSettings> {
  public PerforceOnlyDatesVersionFilterComponent() {
    super(true);
    disableVersionNumbers();
  }

  @Override
  public @NotNull JComponent getComponent() {
    return (JPanel) getStandardPanel();
  }
}
